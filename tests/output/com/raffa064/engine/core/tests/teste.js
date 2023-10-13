export Component::speed = FLOAT;

Component::ready() {
	$transform = $obj.get('Transform2D')
}

Component::process(delta) {
	$transform.pos.x += $speed
}

Component::exit() {
	
}
