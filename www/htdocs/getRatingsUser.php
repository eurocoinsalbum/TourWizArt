<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/rating.inc.php");

$userUuid = httpGetUuidParam("userUuid", true);

$pdo = dbConnectDB();
$ratingRecords = dbGetRatings($pdo, $userUuid);
$output = array("ratings" => $ratingRecords);
print(json_encode ($output));

?>
