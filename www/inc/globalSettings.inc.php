<?php

class GlobalSettings {
	//public $serverUrl = 'http://cdv.homepage-master.de';

	public $serverUrl = 'http://localhost';
}

function isLocalServer() {
	global $globalSettings;
	return $globalSettings->serverUrl == 'http://localhost';
}

$globalSettings = new GlobalSettings();

?>