<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tour.inc.php");

$pdo = dbConnectDB();

$ratingRecords = dbGetRecords($pdo, "rating", array(), null, null);
print("<table>\n");
foreach ($ratingRecords as $ratingRecord) {
	$datasetObjectRecord = dbGetRecord($pdo, "dataset_object", array("datasetObjectUuid" => $ratingRecord["itemUuid"]), null, null);
	if ($datasetObjectRecord == null) {
		continue;
	}
	$textRecord = dbGetRecord($pdo, "text", array("textId" => $datasetObjectRecord["titleTextId"], "languageCode" => LANGUAGE_CODE_EN), null, null);
	print("<tr>\n");
	print("<td>".$textRecord["text"]."</td><td>".$ratingRecord["rating"]."</td>");
	print("</tr>\n");
}
print("</table>\n");

?>
