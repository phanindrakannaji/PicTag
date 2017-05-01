
<?php

include "dbinfo.inc";

$data = json_decode(file_get_contents('php://input'), true);

$user_id = $data["user_id"];
$email = $data["email"];
$firstName = $data["firstName"];
$lastName = $data["lastName"];
$fb_profile_id = $data["fbProfileId"];
$dob = $data["dob"];
$gender = $data["gender"];
$profilePicUrl = $data["profilePicUrl"];

$table = "users";

#Connect to MySql server
$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}
$email = $mysqli->real_escape_string($email);
$profilePicUrl = $mysqli->real_escape_string($profilePicUrl);

$error = "";
$errorOccurred = false;

# Check if user exists
$res = $mysqli->query("SELECT email FROM ".$table." WHERE user_id = $user_id");
if ($res || $res->num_rows != 0 ){
	$query = "UPDATE $table SET firstName = '$firstName', lastName = '$lastName', fb_profile_id = '$fb_profile_id', dob = '$dob', gender = '$gender', profilePicUrl = '$profilePicUrl', last_updated_date = now()";
	$result = $mysqli->query($query);
	if(!$result)
	{
		$error = "Update: Data was not updated in the table " . $mysqli->error . "\n";
		$errorOccurred = true;
	}
} else{
	$error = "Update: Profile does not exists!!! \n";
	$errorOccurred = true;
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
