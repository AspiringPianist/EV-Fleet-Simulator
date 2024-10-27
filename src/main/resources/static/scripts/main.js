const config = {
    type: Phaser.AUTO,
    width: 1920,
    height: 1080,
    scene: {
        create: create
    }
};

const game = new Phaser.Game(config);

function create() {
    const scene = this;
    
    fetch('/api/map')
        .then(response => response.json())
        .then(mapData => {
            console.log("Map Data:", mapData);
            
            const gridHeight = mapData.length;
            const gridWidth = mapData[0].length;
            const tileSize = Math.min(config.width/gridWidth, config.height/gridHeight);
            mapData.forEach((row, y) => {
                console.log(`Row ${y}:`, row.join(' '));
                row.forEach((cell, x) => {
                    const color = cell ? 0xff0000 : 0x00ff00;
                    scene.add.rectangle(
                        x * tileSize + tileSize/2,
                        y * tileSize + tileSize/2,
                        tileSize,
                        tileSize,
                        color
                    );
                });
            });
        });
}
