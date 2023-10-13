function createTile(x, y) {
	const transform = Component.create(_Transform2D)
	const image = Component.create(_Image)
	const tile = Component.create(_Tile)
    
    image.columns = 4
    image.rows = 1
    image.texturePath = 'tiles-ss.png'
    
    tile.x = x
    tile.y = y
    
    const tileObj = Scene.createObject('tile'+x+'x'+y, transform, tile, image)
    Scene.addToScene(tileObj)
    
    return tileObj
}

Tile::ready() {
	$transform = $obj.get(_Transform2D)
	$image = $obj.get(_Image)
}

Tile::process(delta) {
	const tileId = map[map.length - 1 - $y][$x]
	$transform.pos.set($x * tileSize,  $y * tileSize)
	$transform.scale.set(tileSize / 16,  tileSize / 16)
    $image.frame = tileId
    
    if (tileId == P) {
    	Scene.getCamera().position.set($transform.pos.x, $transform.pos.y, 0)
        
        var moveX = Input.keyPressed(Input.LEFT) ? -1 : Input.keyPressed(Input.RIGHT)? 1 : 0
        var moveY = Input.keyPressed(Input.UP) ? -1 : Input.keyPressed(Input.DOWN)? 1 : 0
        
        if ((moveX || moveY) && moveTimer < 0) {
        	moveTimer = .2
            map[map.length - 1 - $y][$x] = G
            map[map.length - 1 - $y + moveY][$x+moveX] = P
        }
    }
}

Tile::exit() {}