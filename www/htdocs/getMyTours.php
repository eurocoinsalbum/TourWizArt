<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tour.inc.php");

$pdo = dbConnectDB();

$tourRecords = dbGetToursByAuthor($pdo, $userUuid);

$output = array("tours" => $tourRecords);
print(json_encode ($output));

?>
