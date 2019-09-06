<?php

define ("LANGUAGE_CODE_EN", "en");
define ("LANGUAGE_CODE_DE", "de");

$http400 = "HTTP/1.0 400 Bad Request";
$http401 = "HTTP/1.0 401 Unauthorized";
$http500 = "HTTP/1.0 500 Internal Server Error";

function httpGetUserUuid() {
	return httpGetUuidParam("userUuid", true);
}

function httpGetTourDatasetUuid() {
	return httpGetUuidParam("tourDatasetUuid", true);
}

function httpGetTourUuid() {
	return httpGetUuidParam("tourUuid", true);
}

function httpGetTourObjectUuid() {
	return httpGetUuidParam("tourObjectUuid", true);
}

function httpGetPlanedTourUuid() {
	return httpGetUuidParam("planedTourUuid", true);
}

function httpGetUuidParam($param, $raiseOnEmpty) {
	global $http400;
	$uuid = httpGetStringParam($param, $raiseOnEmpty);
	if ($uuid == null) {
		if ($raiseOnEmpty) {
			header($http400);
			die("id $uuid invalid");
		}
		return null;
	}
	$uuidRegex = "/^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-4[0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}$/";
	if (preg_match($uuidRegex, $uuid) != 1) {
		header($http400);
		die("id $uuid invalid");
	}
	return $uuid;
}

function httpGetIntParam($param, $raiseOnEmpty) {
	return httpGetStringParam($param, $raiseOnEmpty);
}

function httpGetStringParam($param, $raiseOnEmpty) {
	global $http400;
	if (!isset($_GET[$param])) {
		if (!isset($_POST[$param])) {
			if ($raiseOnEmpty) {
				header($http400);
				die("no param '$param'");
			}
			return null;
		}
		$value = $_POST[$param];
	} else {
		$value = $_GET[$param];
	}
	return $value;
}

function httpGetLanguageCode() {
	global $http400;
	$languageCode = httpGetStringParam("languageCode", true);
	if (!in_array($languageCode, array(LANGUAGE_CODE_EN, LANGUAGE_CODE_DE))) {
		header($http400);
		die("language '$languageCode' invalid");
	}
	return $languageCode;
}

function httpGetBooleanWithDefault($param, $default) {
	global $http400;
	if (!isset($_GET[$param])) {
		return $default;
	}
	return $_GET[$param] == "true";
}

function httpGetBoolean($param) {
	global $http400;
	if (!isset($_GET[$param])) {
		header($http400);
		die("missing parameter");
	}
	return $_GET[$param] == "true";
}

function httpGetTimestampString($paramName) {
	global $http400;
	if (!isset($_GET[$paramName])) {
		header($http400);
		die("no timestamp");
	}
	$timestamp = rawurldecode($_GET[$paramName]);
	$regex = "/\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d([+-][0-4]\d|Z)/";
	if (preg_match($regex, $timestamp) != 1) {
		header($http400);
		die("timestamp invalid $timestamp");
	}
	return $timestamp;
}

function httpGetUploadFilename() {
	return basename($_FILES['upload_file']['name']);
}

?>