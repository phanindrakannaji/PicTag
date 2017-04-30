
<?php

include "dbinfo.inc";

$data = json_decode(file_get_contents('php://input'), true);

$email = $data["email"];

$table = "users";

#Connect to MySql server
$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}
$email = $mysqli->real_escape_string($email);
$error = "";
$errorOccurred = false;

# Check if user exists
$res = $mysqli->query("SELECT email FROM ".$table." WHERE LOWER(email) = LOWER('$email')");
if (!$res || $res->num_rows == 0 ){
	$error = "Login: Email does not exist!!! \n";
	$errorOccurred = true;
} else{
	$res = $mysqli->query("UPDATE $table SET last_login_time=now() WHERE LOWER(email) = LOWER('$email')");
	if (!$res){
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
	$res = $mysqli->query("SELECT user_id, email, firstName, lastName, fb_profile_id, dob, gender, created_date, last_updated_date, last_login_time, reputation, profilePicUrl, lastAlertedTime, token
	  FROM " .$table. " WHERE LOWER(email) = LOWER('$email') ORDER BY email ASC");
	while ($row = $res->fetch_assoc()) {
		$jsonArr["status"] = "S";
		$jsonArr["user_id"] = $row['user_id'];
	    $jsonArr["email"] = $row['email'];
	    $jsonArr["firstName"] = $row['firstName'];
	    $jsonArr["lastName"] = $row['lastName'];
	    $jsonArr["fb_profile_id"] = $row['fb_profile_id'];
	    $jsonArr["dob"] = $row['dob'];
	    $jsonArr["reputation"] = $row['reputation'];
	    $jsonArr["profilePicUrl"] = $row['profilePicUrl'];
	    $jsonArr["token"] = $row['token'];
	    $jsonArr["gender"] = $row['gender'];
	    $jsonMainArr[] = $jsonArr;
	}
}

header('Content-type: application/json');
echo json_encode($jsonMainArr);

$mysqli->close();


?>
