const config = {
    type: Phaser.AUTO,
    width: 1200,
    height: 800,
    parent: 'game-container',
    fps: 30,
    backgroundColor: 0xffffff,  // Add white background
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
const SIGNAL_CHANGE_INTERVAL = 2499;
let lastSignalUpdate = 0;
const EV_UPDATE_INTERVAL = 250;
const TWEEN_DURATION = 500;

function create() {
    scene = this;
    scene.mapLayer = scene.add.graphics();
    scene.signalLayer = scene.add.graphics();
    
    scene.evContainer = scene.add.container(0, 0);
    scene.trafficContainer = scene.add.container(0, 0);
    
    scene.mapLayer.setDepth(1);
    scene.signalLayer.setDepth(2);
    scene.evContainer.setDepth(3);
    scene.trafficContainer.setDepth(4);
    
    // Modify loadMap to return the Promise
    loadMap().then(() => {
        loadExistingEVs();
        updateTrafficSignals();
    }).catch(error => handleFetchError(error, 'initialization'));
    
    setupEventListeners();
}
function updateTrafficSignals() {
    fetch('/api/ev/traffic/signals')
        .then(response => response.json())
        .then(signals => {
            updateTrafficLights(signals);
        })
        .catch(error => handleFetchError(error, 'updateTrafficSignals'));
}

function update(time) {
    if (time - lastUpdateTime >= EV_UPDATE_INTERVAL) {
        updateEVPositions();
        lastUpdateTime = time;
    }

    if (time - lastSignalUpdate >= SIGNAL_CHANGE_INTERVAL) {
        updateTrafficSignals();
        lastSignalUpdate = time;
    }
}

function updateTrafficLights(signals) {
    signals.forEach(signal => {
        let sprite = trafficLightSprites.get(`${signal.x},${signal.y}`);
        if (!sprite) {
            sprite = createTrafficLight(signal);
        }
        const color = signal.isGreen ? 0x00ff00 : 0xff0000;
        sprite.light.setFillStyle(color, 1);
        sprite.glow.setFillStyle(color, 0.3);
        scene.signalLayer.clear();
    });
}

function createTrafficLight(signal) {
    const tileSize = 20;
    const x = signal.y * tileSize + tileSize/2;
    const y = signal.x * tileSize + tileSize/2;
    
    const glow = scene.add.circle(x, y, 12, 0x00ff00, 0.3);
    const light = scene.add.circle(x, y, 6, 0x00ff00);
    
    glow.setDepth(1);
    light.setDepth(2);
    
    const sprite = {light, glow};
    trafficLightSprites.set(`${signal.x},${signal.y}`, sprite);
    return sprite;
}

function createEVSprite(ev) {
    const tileSize = 20;
    const radius = 8;
    
    // Create circle for EV
    const sprite = scene.add.circle(
        ev.currentX * tileSize + tileSize/2,
        ev.currentY * tileSize + tileSize/2,
        radius,
        0x00FF00  // Green color
    );
    
    sprite.setDepth(3);
    scene.evContainer.add(sprite);
    
    return sprite;
}

function getEVTypeColor(type) {
    switch(type) {
        case 1: return 0xFF0000;
        case 2: return 0x00FF00;
        default: return 0x0000FF;
    }
}

function updateEVPositions() {
    fetch('/api/ev/all')
        .then(response => response.json())
        .then(evs => {
            if (!Array.isArray(evs)) {
                console.error('Invalid EVs data:', evs);
                return;
            }
            evs.forEach(ev => {
                if (!ev || !ev.name) {
                    console.error('Invalid EV object:', ev);
                    return;
                }
                const sprite = evSprites.get(ev.name);
                if (sprite) {
                    const tileSize = 20;
                    const newX = ev.currentX * tileSize + tileSize/2;
                    const newY = ev.currentY * tileSize + tileSize/2;
                    
                    scene.tweens.add({
                        targets: sprite,
                        x: newX,
                        y: newY,
                        duration: TWEEN_DURATION,
                        ease: 'Linear'
                    });
                }
            });
        })
        .catch(error => handleFetchError(error, 'updateEVPositions'));
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
    })
    .catch(error => handleFetchError(error, 'checkAndMoveEV'));
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
            fetch(`/api/ev/${evName}/updatePosition`, { 
                method: 'POST' 
            })
            .then(() => checkAndMoveEV(evName, sprite))
            .catch(error => handleFetchError(error, 'moveEV'));
        }
    });
}

function loadMap() {
    // Return the Promise chain
    return fetch('/api/map')
        .then(response => response.json())
        .then(mapData => {
            console.log('Map data received:', mapData);
            if (!mapData || !mapData.roads) {
                throw new Error('Invalid map data received');
            }
            drawMap(scene, mapData);
            return mapData; // Return the data for chaining
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
        })
        .catch(error => handleFetchError(error, 'loadExistingEVs'));
}

function setupEventListeners() {
    document.getElementById('create-ev')?.addEventListener('click', function() {
        const evData = {
            name: document.getElementById('ev-name').value,
            startX: parseInt(document.getElementById('start-point').value.split(',')[0]),
            startY: parseInt(document.getElementById('start-point').value.split(',')[1]),
            endX: parseInt(document.getElementById('end-point').value.split(',')[0]),
            endY: parseInt(document.getElementById('end-point').value.split(',')[1]),
            type: parseInt(document.getElementById('ev-type').value),
            charge: 100, // Default value
            chargingRate: 10 // Default value
        };

        fetch('/api/ev/new', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(evData)
        })
        .then(response => response.json())
        .then(ev => {
            const sprite = createEVSprite(ev);
            addEVToList(ev);
        })
        .catch(error => handleFetchError(error, 'createEV'));
    });
}
function startEVSimulation(evName) {
    fetch(`/api/ev/${evName}/start`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(path => {
        const sprite = evSprites.get(evName);
        if (sprite) {
            sprite.pathData = path;
            sprite.currentPathIndex = 0;
            sprite.isMoving = true;
            sprite.setVisible(true);
            sprite.setDepth(3);
            checkAndMoveEV(evName, sprite);
        }
    })
    .catch(error => handleFetchError(error, 'startEVSimulation'));
}

function stopEVSimulation(evName) {
    const sprite = evSprites.get(evName);
    if (sprite) {
        sprite.isMoving = false;
        fetch(`/api/ev/${evName}/stop`, { method: 'POST' })
            .catch(error => handleFetchError(error, 'stopEVSimulation'));
    }
}

function drawMap(scene, mapData) {
    scene.mapLayer.clear();
    const tileSize = 20;
    
    // Set line style for better visibility
    scene.mapLayer.lineStyle(2, 0x000000);
    
    mapData.roads.forEach(road => {
        // Draw road tiles
        scene.mapLayer.strokeRect(
            road.x * tileSize,
            road.y * tileSize,
            tileSize,
            tileSize
        );
        // Add fill for better visibility
        scene.mapLayer.fillStyle(0xCCCCCC, 0.5);
        scene.mapLayer.fillRect(
            road.x * tileSize,
            road.y * tileSize,
            tileSize,
            tileSize
        );
    });
}
function handleFetchError(error, operation) {
    console.error(`Error during ${operation}:`, error);
}

function createTrafficLight(node) {
    const tileSize = 20;
    const lightSize = 6;
    
    const container = scene.add.container(node.y * tileSize + tileSize/2, node.x * tileSize + tileSize/2);
    
    const light = scene.add.circle(0, 0, lightSize, 0xff0000);
    const glow = scene.add.circle(0, 0, lightSize * 2, 0xff0000, 0.3);
    
    container.add([glow, light]);
    container.setDepth(2);
    
    const sprite = { light, glow, container };
    trafficLightSprites.set(`${node.x},${node.y}`, sprite);
    return sprite;
}

function addEVToList(ev) {
    const evList = document.getElementById('active-evs-list');
    if (!evList) return;

    const li = document.createElement('li');
    li.setAttribute('data-ev-name', ev.name);
    li.innerHTML = `
        <div class="ev-info">
            <span class="ev-name">${ev.name}</span>
            <span class="ev-type">(Type: ${ev.type})</span>
            <span class="ev-charge">Charge: ${ev.charge}%</span>
            <span class="ev-position">Position: (${ev.currentX}, ${ev.currentY})</span>
        </div>
        <div class="ev-controls">
            <button onclick="startEVSimulation('${ev.name}')">Start</button>
            <button onclick="stopEVSimulation('${ev.name}')">Stop</button>
            <button onclick="deleteEV('${ev.name}')">Delete</button>
        </div>
    `;
    evList.appendChild(li);
}

function deleteEV(evName) {
    fetch(`/api/ev/${evName}`, {
        method: 'DELETE'
    })
    .then(() => {
        const sprite = evSprites.get(evName);
        if (sprite) {
            sprite.destroy();
            evSprites.delete(evName);
        }
        document.querySelector(`li[data-ev-name="${evName}"]`)?.remove();
    })
    .catch(error => handleFetchError(error, 'deleteEV'));
}

function handleFetchError(error, context) {
    console.error(`Error in ${context}:`, error);
}

function updateEVStatus(evName) {
    fetch(`/api/ev/${evName}/status`)
        .then(response => response.json())
        .then(status => {
            const evElement = document.querySelector(`li[data-ev-name="${evName}"]`);
            if (evElement) {
                evElement.querySelector('.ev-charge').textContent = `Charge: ${status.charge}%`;
                evElement.querySelector('.ev-position').textContent = 
                    `Position: (${status.currentX}, ${status.currentY})`;
            }
        })
        .catch(error => handleFetchError(error, 'updateEVStatus'));
}

// Initialize simulation
function initializeSimulation() {
    // Create initial test EVs like in TerminalSimulation
    const testEVs = [
        {
            name: "EV1",
            startX: 4,
            startY: 35,
            endX: 35,
            endY: 2,
            type: 1,
            charge: 100,
            chargingRate: 10
        },
        {
            name: "EV2",
            startX: 2,
            startY: 35,
            endX: 35,
            endY: 2,
            type: 2,
            charge: 100,
            chargingRate: 10
        }
    ];

    testEVs.forEach(evData => {
        fetch('/api/ev/new', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(evData)
        })
        .then(response => response.json())
        .then(ev => {
            const sprite = createEVSprite(ev);
            addEVToList(ev);
            startEVSimulation(ev.name);
        })
        .catch(error => handleFetchError(error, 'initializeTestEVs'));
    });
}

// Export necessary functions for global access
window.startEVSimulation = startEVSimulation;
window.stopEVSimulation = stopEVSimulation;
window.deleteEV = deleteEV;

// Initialize when window loads
window.addEventListener('load', initializeSimulation);