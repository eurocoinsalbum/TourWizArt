<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");

function dbGetRating($pdo, $userUuid, $itemUuid) {
	return dbGetRecord($pdo, "rating", array("userUuid" => $userUuid, "itemUuid" => $itemUuid), null);
}

function dbGetRatings($pdo, $userUuid) {
	return dbGetRecords($pdo, "rating", array("userUuid" => $userUuid), null, null);
}

function dbInsertRating($pdo, $userUuid, $itemUuid, $rating) {
	global $http500;
	
	$statement = $pdo->prepare("INSERT INTO rating (userUuid, itemUuid, rating, timestamp) VALUES (:userUuid, :itemUuid, :rating, NOW())");
	$statement->bindParam(":userUuid", $userUuid);
	$statement->bindParam(":itemUuid", $itemUuid);
	$statement->bindParam(":rating", $rating);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die(json_encode(array("error" => "insert rating failed")));
	}
	return TRUE;
}

function dbUpdateRating($pdo, $userUuid, $itemUuid, $rating) {
	global $http500;
	
	$statement = $pdo->prepare("UPDATE rating SET rating=:rating, timestamp=NOW() WHERE userUuid=:userUuid AND itemUuid=:itemUuid");
	$statement->bindParam(":userUuid", $userUuid);
	$statement->bindParam(":itemUuid", $itemUuid);
	$statement->bindParam(":rating", $rating);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die(json_encode(array("error" => "update rating failed")));
	}
	return TRUE;
}

?>
