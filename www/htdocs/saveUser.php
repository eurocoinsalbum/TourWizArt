<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/user.inc.php");

$userUuid = httpGetUuidParam("userUuid", true);
$name = httpGetStringParam("userName", true);

$pdo = dbConnectDB();

$existingUserRecord = dbGetUser($pdo, $userUuid);
$roleUser = ROLE_USER;
if (empty($existingUserRecord)) {
	$success = dbInsertUser($pdo, $userUuid, $name, ROLE_USER);
} else {
	$roleUser = $existingUserRecord["role"];
	$success = true;
}

$output = array("role" => $roleUser);
print(json_encode ($output));

?>
