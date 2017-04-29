
<?php

include "dbinfo.inc";

$data = json_decode(file_get_contents('php://input'), true);

$latitude = floatval(0);
$longitude = floatval(0);
$email = $data["email"];
$password = $data["password"];

$table = "friends";

#Connect to MySql server
$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}
$email = $mysqli->real_escape_string($email);
$password = $mysqli->real_escape_string($password);
$error = "";
$errorOccurred = false;

# Check if user exists
$res = $mysqli->query("SELECT email FROM ".$table." WHERE LOWER(email) = LOWER('$email')");
if (!$res || $res->num_rows == 0 ){
	$error = "Login: Email does not exist!!! \n";
	$errorOccurred = true;
} else{
	$res = $mysqli->query("SELECT email FROM ".$table." WHERE LOWER(email) = LOWER('$email') and password = '$password'");
	if (!$res || $res->num_rows == 0 ){
		$error = "Login: No matching account for email and password!! \n";
		$errorOccurred = true;
	}
}

$jsonMainArr = array();
if ($errorOccurred){
	$jsonArr["status"] = "F";
	$jsonArr["errorMessage"] = $error;
	$jsonMainArr[] = $jsonArr;
} else{
	$res = $mysqli->query("SELECT email, fullName, latestTimestamp, latitude, longitude  FROM " .$table. " WHERE LOWER(email) = LOWER('$email') ORDER BY email ASC");
	while ($row = $res->fetch_assoc()) {
		$jsonArr["status"] = "S";
	    $jsonArr["email"] = $row['email'];
	    $jsonArr["fullName"] = $row['fullName'];
	    $jsonArr["latestTimestamp"] = $row['latestTimestamp'];
	    $jsonArr["latitude"] = $row['latitude'];
	    $jsonArr["longitude"] = $row['longitude'];
	    $jsonMainArr[] = $jsonArr;
	}
}

header('Content-type: application/json');
echo json_encode($jsonMainArr);

$mysqli->close();


?>
