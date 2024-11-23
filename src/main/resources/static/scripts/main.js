const config = {
    type: Phaser.AUTO,
    width: 1200,
    height: 800,
    parent: 'game-container',
    scene: {
        create: create,
        update: update
    }
};

const game = new Phaser.Game(config);
let evSprites = new Map();
let trafficLightSprites = new Map();
let scene;

function create() {
    scene = this;
    scene.mapLayer = scene.add.graphics();
    scene.signalLayer = scene.add.graphics();
    
    loadMap();
    setupEventListeners();
    loadExistingEVs();
    startTrafficSignalUpdates();
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
    ).setDepth(2);
    evSprites.set(ev.name, sprite);
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
        simulateEVMovement(evName, path);
    });
}

function simulateEVMovement(evName, path) {
    const sprite = evSprites.get(evName);
    let pathIndex = 0;

    function moveStep() {
        if (pathIndex >= path.length - 1) return;
        
        fetch(`/api/ev/${evName}/canMove`, {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    console.error('Error response:', text);
                    throw new Error(text);
                });
            }
            return response.json();
        })
        .then(canMove => {
            console.log(`Movement check for ${evName}: ${canMove}`);
            if (canMove) {
                const nextPos = path[pathIndex + 1];
                scene.tweens.add({
                    targets: sprite,
                    x: nextPos.y * 20 + 10,
                    y: nextPos.x * 20 + 10,
                    duration: 10,
                    ease: 'Linear',
                    onComplete: () => {
                        pathIndex++;
                        moveStep();
                    }
                });
            } else {
                setTimeout(moveStep, 100);
            }
        })
        .catch(error => console.error('Movement error:', error));
    }
    
    moveStep();
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

function startTrafficSignalUpdates() {
    console.log('Starting traffic signal updates');
    updateTrafficSignals();
    // Update more frequently for smoother transitions
    setInterval(updateTrafficSignals, 1000); // Check every second
}

function updateTrafficSignals() {
    fetch('/api/ev/traffic/signals')
        .then(response => response.json())
        .then(signals => {
            signals.forEach(signal => {
                let sprite = trafficLightSprites.get(`${signal.x},${signal.y}`);
                if (!sprite) {
                    // Create traffic light with enhanced visibility
                    sprite = scene.add.circle(
                        signal.y * 20 + 10,
                        signal.x * 20 + 10,
                        5,
                        signal.isGreen ? 0x00ff00 : 0xff0000
                    ).setDepth(2);
                    
                    // Add outer ring for glow effect
                    const glowRing = scene.add.circle(
                        signal.y * 20 + 10,
                        signal.x * 20 + 10,
                        8,
                        signal.isGreen ? 0x00ff00 : 0xff0000,
                        0.3
                    ).setDepth(1);
                    
                    trafficLightSprites.set(`${signal.x},${signal.y}`, {
                        light: sprite,
                        glow: glowRing
                    });
                } else {
                    const color = signal.isGreen ? 0x00ff00 : 0xff0000;
                    sprite.light.setFillStyle(color);
                    sprite.glow.setFillStyle(color);
                }
            });
        });
}
function startTrafficSignalUpdates() {
    updateTrafficSignals();
    setInterval(() => {
        fetch('/api/ev/traffic/change', { method: 'POST' })
            .then(() => updateTrafficSignals());
    }, 10000);
}



function update() {
    // Real-time updates if needed
}
