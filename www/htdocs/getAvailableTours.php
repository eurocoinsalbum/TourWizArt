<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tour.inc.php");

define("LIMIT_TOURS", 100);

$tourDatasetUuid = httpGetTourDatasetUuid();
$pdo = dbConnectDB();

$tourRecords = dbGetToursByTourDataset($pdo, $tourDatasetUuid, LIMIT_TOURS);
$output = array("tours" => $tourRecords);
print(json_encode ($output));

?>
