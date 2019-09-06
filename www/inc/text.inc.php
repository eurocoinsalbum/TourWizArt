<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");

function dbGetPeople($pdo) {
	return dbGetRecords($pdo, "people", array(), null, null);
}

function dbGetTexts($pdo, $languageCode) {
	// multi language not support now
	$languageCode = LANGUAGE_CODE_EN;
	return dbGetRecords($pdo, "text", array("languageCode" => $languageCode), null, null);
}

function dbCreateTextId($pdo, $languageCode, $text) {
	global $http500;
	$statement = $pdo->prepare("INSERT INTO text (languageCode, text) VALUES (:languageCode, :text)");
	$statement->bindParam(":languageCode", $languageCode);
	$statement->bindParam(":text", $text);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die("insert text failed");
	}
	return $pdo->lastInsertId();
}

function dbUpdateTextId($pdo, $textId, $languageCode, $text) {
	global $http500;
	$statement = $pdo->prepare("UPDATE text SET text=:text WHERE textId=:textId AND languageCode=:languageCode");
	$statement->bindParam(":textId", $textId);
	$statement->bindParam(":languageCode", $languageCode);
	$statement->bindParam(":text", $text);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die("update text failed");
	}
}

function dbDeleteTextId($pdo, $textId) {
	global $http500;
	$statement = $pdo->prepare("DELETE FROM text WHERE textId=:textId");
	$statement->bindParam(":textId", $textId);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die("delete text failed");
	}
}

?>
