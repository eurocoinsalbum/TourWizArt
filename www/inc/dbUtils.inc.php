<?php
$rootDir = "..";
require_once("$rootDir/inc/globalSettings.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");

class DbCredentials {
	public $dbHost = 'secret';
	public $dbName = 'secret';
	public $dbUser = 'secret';
	public $dbPassword = 'secret';

	public function __construct() {	
		if (isLocalServer()) {
			$this->dbHost = 'localhost';
			$this->dbName = 'tour';
			$this->dbUser = 'tour';
			$this->dbPassword = 'tour';
		}
	}
}

$dbCredentials = new DbCredentials();
$userUuid = null;

function dbConnectDB() {
	global $dbCredentials;
	global $userUuid;

	// get userUuid from URL
	$userUuid = httpGetUserUuid();
	if ($userUuid == null) {
		return null;
	}

	// connect database
	$pdo = new PDO("mysql:host=$dbCredentials->dbHost;dbname=$dbCredentials->dbName", $dbCredentials->dbUser, $dbCredentials->dbPassword, [PDO::MYSQL_ATTR_INIT_COMMAND => "SET time_zone='+00:00'"]);
	if (isLocalServer()) {
		$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_WARNING);
	}
	return $pdo;
}

function dbBeginTransaction($pdo) {
	$pdo->beginTransaction();
}

function dbCommitTransaction($pdo) {
	$pdo->commit();
}

function dbGetRecord($pdo, $tablename, $bindings, $additionalWherePart) {
	$records = dbGetRecords($pdo, $tablename, $bindings, $additionalWherePart, 1);
	if (empty($records)) {
		return array();
	}
	return $records[0];
}

function dbGetRecords($pdo, $tablename, $bindings, $additionalWherePart, $limit) {
	global $http500;
	$rows = array();
	$sql = "SELECT * FROM $tablename";
	$nextSqlInterWord = "WHERE";
	foreach ($bindings as $name => $value) {
		$sql .= " $nextSqlInterWord $name=:$name";
		$nextSqlInterWord = "AND";
	}
	if ($additionalWherePart != null) {
		$sql .= " $nextSqlInterWord $additionalWherePart";
	}
	
	if ($limit != null) {
		$sql .= " LIMIT ".$limit;
	}
	
	$statement = $pdo->prepare($sql);
	foreach ($bindings as $name => $value) {
		$statement->bindParam(":$name", $bindings[$name]);
	}
	$result = $statement->execute();
	if ($result === FALSE) {
		header($http500);
		die("get $tablename error 1");
	}
	if ($statement->rowCount() == 0) {
		return $rows;
	}

	while ($row = $statement->fetch(PDO::FETCH_ASSOC)) {
		if ($row === FALSE) {
			header($http500);
			die("get $tablename error 2");
		}
		array_push($rows, $row);
	}
	return $rows;
}

function getIntFromBool($value) {
	return $value ? 1 : 0;
}

function getBoolFromInt($value) {
	return $value != 0;
}

function formatDateTime($dateTime) {
	return $dateTime->format('Y-m-d\TH:i:sZ');
}

function getNowMinusNDays($days) {
	$expires = new DateTime("NOW");
	$expires->sub(new DateInterval("P".$days."D"));
	return $expires;
}

function getNowPlusNDays($days) {
	$expires = new DateTime("NOW");
	$expires->add(new DateInterval("P".$days."D"));
	return $expires;
}

function parseDateTime($value) {
	return date_create_from_format('Y-m-dTH:i:sZ', $value);
}
?>
