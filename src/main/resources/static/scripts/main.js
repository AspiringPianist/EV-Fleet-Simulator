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
let scene;

function create() {
    scene = this;
    scene.graphics = scene.add.graphics();
    
    loadMap();
    setupEventListeners();
    loadExistingEVs();
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
            } else {
                console.log('No EVs found');
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
    if (!ev || !ev.startX || !ev.startY) {
        console.log("Invalid EV data received:", ev);
        return;
    }
   
    const sprite = scene.add.circle(
        (ev.startY * 20) + 10,  // Add half tile size (10) to center in the cell
        (ev.startX * 20) + 10,  // Add half tile size (10) to center in the cell
        5,
        getEVTypeColor(ev.type)
    );
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
    }
    
    moveStep();
}

function drawMap(scene, mapData) {
    scene.graphics.clear();
    const tileSize = 20;

    mapData.roads.forEach(road => {
        const color = road.oneWay ? 0x444444 : 0x666666;
        scene.graphics.lineStyle(2, color);
        scene.graphics.strokeRect(
            road.y * tileSize,
            road.x * tileSize,
            tileSize,
            tileSize
        );
    });
}

function update() {
    // Real-time updates if needed
}
