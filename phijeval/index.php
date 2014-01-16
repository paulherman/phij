<?php
require_once("mp.php");
$data = $_GET;
if ((isset($data['appKey']) == true) && ($data['appKey'] == "e5ee871ea55fada67f16b1ddf89087aa997822a1"))
{
	$json = json_decode($data['json'], true);
	$expression = $json['expression'];
	$args = $json['args'];
	$parser = new CMParser();
	$parser->load($expression);
	foreach ($args as $key => $value)
		$parser->setParameter($key, $value);
	$result = $parser->evaluate();
	if ($result !== false)
		echo $result;
}
?>
