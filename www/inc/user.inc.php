<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");

define("ROLE_ADMIN", "ADMIN");
define("ROLE_USER", "USER");
define("ROLE_INSTITUTE", "INSTITUTE");

function dbGetUsers($pdo) {
	return dbGetRecords($pdo, "user", array(), null, null);
}

function dbGetUser($pdo, $userUuid) {
	return dbGetRecord($pdo, "user", array("userUuid" => $userUuid), null);
}

function dbInsertUser($pdo, $userUuid, $name, $role) {
	global $http500;
	
	$statement = $pdo->prepare("INSERT INTO user (userUuid, name, role) VALUES (:userUuid, :name, :role)");
	$statement->bindParam(":userUuid", $userUuid);
	$statement->bindParam(":name", $name);
	$statement->bindParam(":role", $role);
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die(json_encode(array("error" => "insert user failed")));
	}
	return TRUE;
}

?>
