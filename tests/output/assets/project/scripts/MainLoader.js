const W = 0
const B = 1
const P = 2
const G = 3

const map = [
	[W, W, W, W, W],
	[W, B, G, G, W],
	[W, G, G, G, W],
	[W, G, G, P, W],
	[W, W, W, W, W]
]

const tileSize = 60
var moveTimer = 0;

MainLoader::ready() {
	for (var y = 0; y < map.length; y++) {
		for (var x = 0; x < map[y].length; x++) {
        	createTile(x, y)
    	}
    }
}

MainLoader::process(delta) {
	moveTimer -= delta
}

MainLoader::exit() {}