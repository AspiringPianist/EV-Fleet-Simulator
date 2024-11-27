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
const TILE_SIZE = 20;

function create() {
    scene = this;
    scene.mapLayer = scene.add.graphics();
    scene.signalLayer = scene.add.graphics();
    
    loadMap();
    setupEventListeners();
    loadExistingEVs();
    
    // Initial update and start interval
    updateTrafficSignals();
    setInterval(updateTrafficSignals, 1000);
    setInterval(simulateEVMovement, 1000);

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
        const startX = parseInt(document.getElementById('start-point').value.split(',')[0]);
        const startY = parseInt(document.getElementById('start-point').value.split(',')[1]);
        const endX = parseInt(document.getElementById('end-point').value.split(',')[0]);
        const endY = parseInt(document.getElementById('end-point').value.split(',')[1]);

        const requestData = {
            name: document.getElementById('ev-name').value || `EV_${Date.now()}`,
            startX: startX,
            startY: startY,
            endX: endX,
            endY: endY,
            type: parseInt(document.getElementById('ev-type').value),
            charge: 100,
            chargingRate: 10,
            currentPathIndex: 0,
            moving: true
        };
        
        fetch('/api/ev/new', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(requestData)
        })
        .then(response => response.json())
        .then(ev => {
            // Create sprite at the actual start position
            const evSprite = createEVSprite(ev);
            if (evSprite) {
                evSprite.x = startY * TILE_SIZE;
                evSprite.y = startX * TILE_SIZE;
            }
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

function simulateEVMovement() {
    fetch('/api/ev/all')
        .then(response => response.json())
        .then(evs => {
            evs.forEach(ev => {
                if (!ev.path || !ev.moving || ev.currentPathIndex >= ev.path.length - 1) return;
                
                const nextPosition = ev.path[ev.currentPathIndex + 1];
                
                // Ensure coordinates are within valid range (1 to maxDim)
                const x = Math.min(Math.max(nextPosition.x, 1), 35);
                const y = Math.min(Math.max(nextPosition.y, 1), 35);
                
                fetch(`/api/ev/${ev.name}/canMoveToPosition/${x}/${y}`, {
                    method: 'POST'
                })
                .then(response => response.json())
                .then(canMove => {
                    if (canMove) {
                        const evSprite = scene.children.getByName(`ev-${ev.name}`);
                        if (evSprite) {
                            evSprite.x = (y - 1) * TILE_SIZE;
                            evSprite.y = (x - 1) * TILE_SIZE;
                        }
                    }
                })
                .catch(error => console.error('Error checking position:', error));
            });
        })
        .catch(error => console.error('Error fetching EVs:', error));
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

// function startTrafficSignalUpdates() {
//     console.log('Starting traffic signal updates');
//     updateTrafficSignals();
//     // Update more frequently for smoother transitions
//     setInterval(updateTrafficSignals, 2500); // Check every second
// }

function updateTrafficSignals() {
    fetch('/api/ev/traffic/signals')
        .then(response => response.json())
        .then(data => {
            if (!Array.isArray(data)) {
                console.error('Expected array of signals, got:', data);
                return;
            }
            data.forEach(signal => {
                updateTrafficLightUI(signal.x, signal.y, signal.isGreen);
            });
        })
        .catch(error => console.error('Error fetching traffic signals:', error));
}



function updateTrafficLightUI(x, y, isGreen) {
    let sprite = trafficLightSprites.get(`${x},${y}`);
    const color = isGreen ? 0x00ff00 : 0xff0000;
    
    if (!sprite) {
        // Create main light
        const light = scene.add.circle(
            y * TILE_SIZE + 10,
            x * TILE_SIZE + 10,
            5,
            color
        ).setDepth(2);

        // Create glow effect
        const glow = scene.add.circle(
            y * TILE_SIZE + 10,
            x * TILE_SIZE + 10,
            8,
            color,
            0.3
        ).setDepth(1);

        trafficLightSprites.set(`${x},${y}`, {
            light: light,
            glow: glow
        });
    } else {
        // Update existing sprites
        sprite.light.setFillStyle(color);
        sprite.glow.setFillStyle(color);
    }
}
// function startTrafficSignalUpdates() {
//     updateTrafficSignals();
//     setInterval(() => {
//         fetch('/api/ev/traffic/change', { method: 'POST' })
//             .then(() => updateTrafficSignals());
//     }, 2500);
// }



function update() {
    // Real-time updates if needed
}