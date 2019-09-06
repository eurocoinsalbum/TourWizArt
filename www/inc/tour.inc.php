<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");

define("TOUR_STATUS_ACTIVE", "ACTIVE");

define("TOUR_ACCESS_LEVEL_PUBLIC", "PUBLIC");

function dbGetTour($pdo, $tourUuid) {
	return dbGetRecord($pdo, "tour", array("tourUuid" => $tourUuid), null);
}

function dbGetToursByTourDataset($pdo, $tourDatasetUuid, $limit) {
	return dbGetRecords($pdo, "tour", array("tourDatasetUuid" => $tourDatasetUuid, "status" => "ACTIVE"), null, $limit);
}

function dbGetToursByAuthor($pdo, $authorUserUuid) {
	return dbGetRecords($pdo, "tour", array("authorUserUuid" => $authorUserUuid), null, null);
}

function dbGetToursCreatedByUser($pdo, $userUuid) {
	return dbGetRecords($pdo, "tour", array("authorUserUuid" => $userUuid), null, null);
}

function dbGetTourObjects($pdo, $tourUuid) {
	return dbGetRecords($pdo, "tour_object", array("tourUuid" => $tourUuid), null, null);
}

function dbInsertTour($pdo, $tourUuid, $tourDatasetUuid, $authorUserUuid, $titleTextId, $descriptionTextId, $status, $accessLevel, $iconUuid) {
	global $http500;
	
	$statement = $pdo->prepare("INSERT INTO tour (tourUuid, tourDatasetUuid, authorUserUuid, titleTextId, descriptionTextId, lastUpdate, created, accessLevel, status, iconUuid) VALUES (:tourUuid, :tourDatasetUuid, :authorUserUuid, :titleTextId, :descriptionTextId, NOW(), NOW(), :accessLevel, :status, :iconUuid)");
	$statement->bindParam(":tourUuid", $tourUuid);
	$statement->bindParam(":tourDatasetUuid", $tourDatasetUuid);
	$statement->bindParam(":authorUserUuid", $authorUserUuid);
	$statement->bindParam(":titleTextId", $titleTextId);
	$statement->bindParam(":descriptionTextId", $descriptionTextId);
	$statement->bindParam(":accessLevel", $accessLevel);
	$statement->bindParam(":status", $status);
	$statement->bindParam(":status", $status);
	$statement->bindParam(":iconUuid", $iconUuid);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die(json_encode(array("error" => "insert tour failed")));
	}
	return TRUE;
}

function dbUpdateTour($pdo, $tourUuid, $status, $accessLevel, $iconUuid) {
	global $http500;
	
	$statement = $pdo->prepare("UPDATE tour SET lastUpdate=NOW(), accessLevel=:accessLevel, status=:status, iconUuid=:iconUuid WHERE tourUuid=:tourUuid");
	$statement->bindParam(":tourUuid", $tourUuid);
	$statement->bindParam(":accessLevel", $accessLevel);
	$statement->bindParam(":status", $status);
	$statement->bindParam(":iconUuid", $iconUuid);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die(json_encode(array("error" => "update tour failed")));
	}
	return TRUE;
}

function dbDeleteTour($pdo, $tourUuid) {
	global $http500;
	
	$statement = $pdo->prepare("DELETE FROM tour WHERE tourUuid=:tourUuid");
	$statement->bindParam(":tourUuid", $tourUuid);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die(json_encode(array("error" => "delete tour failed")));
	}
	return TRUE;
}

function dbInsertTourObject($pdo, $tourUuid, $sequenceId, $datasetObjectUuid) {
	global $http500;
	
	$statement = $pdo->prepare("INSERT INTO tour_object (tourUuid, datasetObjectUuid, sequenceId) VALUES (:tourUuid, :datasetObjectUuid, :sequenceId)");
	$statement->bindParam(":tourUuid", $tourUuid);
	$statement->bindParam(":datasetObjectUuid", $datasetObjectUuid);
	$statement->bindParam(":sequenceId", $sequenceId);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die(json_encode(array("error" => "insert tour object failed")));
	}
	return TRUE;
}

function dbDeleteTourObjects($pdo, $tourUuid) {
	global $http500;
	
	$statement = $pdo->prepare("DELETE FROM tour_object WHERE tourUuid=:tourUuid");
	$statement->bindParam(":tourUuid", $tourUuid);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die(json_encode(array("error" => "delete tour objects failed")));
	}
	return TRUE;
}

?>
