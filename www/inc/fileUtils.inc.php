<?php

define("X_HEADER_CHAR", "X");
define("X_HEADER_UCID", "U");
define("X_HEADER_OWNER", "O");
define("X_HEADER_COMPARE_BASE_COLLECTION", "B");
define("X_HEADER_COMPARE_COLLECTION", "A");

define("NUMBER_OF_COIN_KEY_TOKENS", 11);

class CollectionHeader {
	public $owner = null;
	public $ucid = null;
	public $ucidCompareBaseCollection = null;
	public $ucidCompareCollection = null;
}

class CollectionStatistics {
	public $totalCoins;
	public $coins = array();
}

function createDirectory($userBackupDir) {
	if (!file_exists($userBackupDir)) {
		mkdir($userBackupDir, 0777, true);
	}
}

function rrmdir($dir) {
	if (is_dir($dir)) {
		$objects = scandir($dir);
		foreach ($objects as $object) {
			if ($object != "." && $object != "..") {
				$fullName = $dir."/".$object;
				if (is_dir($fullName)) {
					rrmdir($fullName);
				} else {
					unlink($fullName);
				}
			}
		}
		rmdir($dir);
	}
}

function isBackup($name) {
	$prefixCheck = "backup_euro_coin_list_";
	$pos = strpos($name, $prefixCheck);
	return $pos !== false && $pos === 0;
}

function printCollection($pdo, $ucid, $hideOwner) {
	global $http500;
	global $rootDir;
	
	$owner = "*";
	if (!$hideOwner) {
		$uuidOwner = dbGetCollectionWithCheck($pdo, $ucid)["uuid"];
		$rowEMailOwner = dbGetEmailByUuidWithCheck($pdo, $uuidOwner);
		$owner = $rowEMailOwner["email"];
	}
	
	$sharesDir = "$rootDir/euroshares";
	$sharedCollectionAbsoluteFilename = "$sharesDir/$ucid";
	if (!file_exists($sharedCollectionAbsoluteFilename)) {
		header($http500);
		die("shared collection file missing $ucid");
	}
	print (X_HEADER_CHAR.X_HEADER_OWNER."$owner\n");
	readfile("$sharedCollectionAbsoluteFilename");
}

function getCollectionHeadersFromFile($absoluteFilename) {
	global $http500;
	if (!file_exists($absoluteFilename)) {
		header($http500);
	}
	$handle = fopen($absoluteFilename, "r");
	if (!$handle) {
		header($http500);
		die("collection file read error");
	}
	$collectionHeader = new CollectionHeader();
	while(($line = fgets($handle)) !== false) {
		if ($line[0] != X_HEADER_CHAR) {
			break;
		}
		$value = trim(substr($line, 2));
		switch($line[1]) {
			case X_HEADER_OWNER:
				$collectionHeader->owner = $value;
				break;
			case X_HEADER_UCID:
				$collectionHeader->ucid = $value;
				break;
			case X_HEADER_COMPARE_BASE_COLLECTION:
				$collectionHeader->ucidCompareBaseCollection = $value;
				break;
			case X_HEADER_COMPARE_COLLECTION:
				$collectionHeader->ucidCompareCollection = $value;
				break;			
		}
	}
	fclose($handle);
	return $collectionHeader;
}

function getCollectionStatisticsFromFile($absoluteFilename) {
	if (!file_exists($absoluteFilename)) {
		header($http500);
		die("collection file missing");
	}
	$handle = fopen($absoluteFilename, "r");
	if (!$handle) {
		header($http500);
		die("collection file read error");
	}
	$collectionStatistics = new CollectionStatistics();
	while(($line = fgets($handle)) !== false) {
		if ($line[0] == X_HEADER_CHAR) {
			continue;
		}

		// <versionCode>;<customCoin>[*|-];<is 2€CC>[2ECC|-];<currency>;<countrycode>;<year>;<common coin title>;<special coin title>;
	    // <coin title id>;<coin value>;<coinImageFilename>;<count>;<date added>;<paid>;<userCoinValue>;<note>;<condition>
		$pos = strposX($line, ";", NUMBER_OF_COIN_KEY_TOKENS);
		$coinKey = substr($line, 0, $pos - 1);
		// TODO check count
		$collectionStatistics->coins[$coinKey] = $line;
	}
	$collectionStatistics->totalCoins = count($collectionStatistics->coins);
	fclose($handle);
	return $collectionStatistics;
}

function strposX($haystack, $needle, $number) {
	preg_match_all("/$needle/", utf8_decode($haystack), $matches, PREG_OFFSET_CAPTURE);
	return $matches[0][$number - 1][1];
}
?>