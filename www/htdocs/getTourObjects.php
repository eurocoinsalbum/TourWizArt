<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tour.inc.php");

$tourUuid = httpGetTourUuid();
$pdo = dbConnectDB();
$tourObjects = dbGetTourObjects($pdo, $tourUuid);
$output = array("tourObjects" => $tourObjects);
print(json_encode ($output));
?>
