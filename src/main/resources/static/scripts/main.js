const config = {
    type: Phaser.AUTO,
    width: 1200,
    height: 800,
    parent: 'game-container',
    fps: 30,
    scene: {
        create: create,
        update: update
    }
};

const game = new Phaser.Game(config);
let evSprites = new Map();
let trafficLightSprites = new Map();
let scene;
let lastUpdateTime = 0;
const SIGNAL_CHANGE_INTERVAL = 1499; // Matches backend interval
let lastSignalUpdate = 0;
const EV_UPDATE_INTERVAL = 300; // More frequent EV position updates
const TWEEN_DURATION = 200; // Longer duration for visible movement

function updateTrafficSignals() {
    fetch('/api/ev/traffic/signals')
        .then(response => response.json())
        .then(signals => {
            signals.forEach(signal => {
                let sprite = trafficLightSprites.get(`${signal.x},${signal.y}`);
                if (!sprite) {
                    sprite = createTrafficLight(signal);
                }
                const color = signal.isGreen ? 0x00ff00 : 0xff0000;
                sprite.light.setFillStyle(color);
                sprite.glow.setFillStyle(color);
                console.log(`Traffic Node (${signal.x},${signal.y}): ${signal.isGreen ? 'GREEN' : 'RED'} - Queue: ${signal.queueSize}`);
            });
        });
}

function create() {
    scene = this;
    scene.mapLayer = scene.add.graphics();
    scene.signalLayer = scene.add.graphics();
    
    loadMap();
    setupEventListeners();
    loadExistingEVs();
    updateTrafficSignals();
}
function update(time) {
    // Update EV positions
    if (time - lastUpdateTime >= EV_UPDATE_INTERVAL) {
        updateEVPositions();
        lastUpdateTime = time;
    }

    // Update traffic signals based on backend state
    if (time - lastSignalUpdate >= SIGNAL_CHANGE_INTERVAL) {
        fetch('/api/ev/traffic/signals')
            .then(response => response.json())
            .then(signals => {
                updateTrafficLights(signals);
                lastSignalUpdate = time;
            });
    }
}


function updateTrafficLights(signals) {
    signals.forEach(signal => {
        let sprite = trafficLightSprites.get(`${signal.x},${signal.y}`);
        if (!sprite) {
            sprite = createTrafficLight(signal);
        }
        const color = signal.isGreen ? 0x00ff00 : 0xff0000;
        sprite.light.setFillStyle(color, 1);  // Added alpha value
        sprite.glow.setFillStyle(color, 0.3);
        scene.signalLayer.clear();  // Clear previous renders
    });
}


function createTrafficLight(signal) {
    const light = scene.add.circle(
        signal.y * 20 + 10,
        signal.x * 20 + 10,
        5,
        signal.isGreen ? 0x00ff00 : 0xff0000
    ).setDepth(1);
    
    const glow = scene.add.circle(
        signal.y * 20 + 10,
        signal.x * 20 + 10,
        8,
        signal.isGreen ? 0x00ff00 : 0xff0000,
        0.3
    ).setDepth(0);
    
    const sprite = { light, glow };
    trafficLightSprites.set(`${signal.x},${signal.y}`, sprite);
    return sprite;
}

function updateEVPositions() {
    evSprites.forEach((sprite, evName) => {
        if (sprite.isMoving && sprite.pathData) {
            checkAndMoveEV(evName, sprite);
        }
    });
}

function checkAndMoveEV(evName, sprite) {
    if (sprite.currentPathIndex >= sprite.pathData.length - 1) {
        sprite.isMoving = false;
        return;
    }

    const nextPos = sprite.pathData[sprite.currentPathIndex + 1];
    fetch(`/api/ev/${evName}/canMoveToPosition/${nextPos.x}/${nextPos.y}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(canMove => {
        if (canMove) {
            moveEV(sprite, nextPos, evName);
        }
    });
}

function moveEV(sprite, nextPos, evName) {
    scene.tweens.add({
        targets: sprite,
        x: nextPos.y * 20 + 10,
        y: nextPos.x * 20 + 10,
        duration: TWEEN_DURATION,
        ease: 'Linear',
        onComplete: () => {
            sprite.currentPathIndex++;
            fetch(`/api/ev/${evName}/updatePosition`, { method: 'POST' });
            // Immediately check for next movement
            checkAndMoveEV(evName, sprite);
        }
    });
}
function checkAndMoveEV(evName, sprite) {
    if (sprite.currentPathIndex >= sprite.pathData.length - 1) {
        sprite.isMoving = false;
        return;
    }

    const nextPos = sprite.pathData[sprite.currentPathIndex + 1];
    fetch(`/api/ev/${evName}/canMoveToPosition/${nextPos.x}/${nextPos.y}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(canMove => {
        if (canMove) {
            moveEV(sprite, nextPos, evName);
        }
    });
}
function loadMap() {
    fetch('/api/map')
        .then(response => response.json())
        .then(mapData => {
            drawMap(scene, mapData);
        });
}

function loadExistingEVs() {
    fetch('/api/ev/all')
        .then(response => response.json())
        .then(data => {
            if (Array.isArray(data)) {
                data.forEach(ev => {
                    createEVSprite(ev);
                    addEVToList(ev);
                });
            }
        });
}

function setupEventListeners() {
    document.getElementById('create-ev').addEventListener('click', () => {
        const requestData = {
            name: document.getElementById('ev-name').value || `EV_${Date.now()}`,
            startX: parseInt(document.getElementById('start-point').value.split(',')[0]),
            startY: parseInt(document.getElementById('start-point').value.split(',')[1]),
            endX: parseInt(document.getElementById('end-point').value.split(',')[0]),
            endY: parseInt(document.getElementById('end-point').value.split(',')[1]),
            type: parseInt(document.getElementById('ev-type').value),
            charge: 100,
            chargingRate: 10
        };
        
        fetch('/api/ev/new', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(requestData)
        })
        .then(response => response.json())
        .then(ev => {
            createEVSprite(ev);
            addEVToList(ev);
        });
    });
}


function createEVSprite(ev) {
    const sprite = scene.add.circle(
        (ev.startY * 20) + 10,
        (ev.startX * 20) + 10,
        5,
        getEVTypeColor(ev.type)
    ).setDepth(3);
    sprite.isMoving = false;
    sprite.currentPathIndex = 0;
    evSprites.set(ev.name, sprite);
    scene.add.existing(sprite);  // Ensure sprite is added to scene
}
function getEVTypeColor(type) {
    const colors = {
        1: 0xff0000,
        2: 0x00ff00,
        3: 0x0000ff
    };
    return colors[type] || 0xff0000;
}

function addEVToList(ev) {
    const evList = document.getElementById('active-evs-list');
    const evElement = document.createElement('div');
    evElement.className = 'ev-item';
    evElement.innerHTML = `
        <h4>${ev.name}</h4>
        <p>Type: ${ev.type}</p>
        <p>Charge: ${ev.charge}%</p>
        <p>Start: (${ev.startX}, ${ev.startY})</p>
        <p>End: (${ev.endX}, ${ev.endY})</p>
        <button onclick="startEVSimulation('${ev.name}')" class="btn btn-primary btn-sm">Start</button>
        <button onclick="stopEVSimulation('${ev.name}')" class="btn btn-danger btn-sm">Stop</button>
    `;
    evList.appendChild(evElement);
}

function startEVSimulation(evName) {
    fetch(`/api/ev/${evName}/start`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(path => {
        const sprite = evSprites.get(evName);
        sprite.pathData = path;
        sprite.currentPathIndex = 0;
        sprite.isMoving = true;
        // Ensure sprite is visible and at correct depth
        sprite.setVisible(true);
        sprite.setDepth(3);
        console.log(`EV ${evName} started simulation with path:`, path);
        // Start movement immediately
        checkAndMoveEV(evName, sprite);
    });
}

function stopEVSimulation(evName) {
    const sprite = evSprites.get(evName);
    if (sprite) {
        sprite.isMoving = false;
    }
}

function drawMap(scene, mapData) {
    scene.mapLayer.clear();
    const tileSize = 20;

    mapData.roads.forEach(road => {
        const color = road.oneWay ? 0x888888 : 0xAAAAAA;
        scene.mapLayer.lineStyle(2, color);
        scene.mapLayer.strokeRect(
            road.y * tileSize,
            road.x * tileSize,
            tileSize,
            tileSize
        );
    });
}
