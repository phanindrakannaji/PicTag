
<?php

include "dbinfo.inc";

function getDistanceFromLatLonInKm($lat1,$lon1,$lat2,$lon2) {
  $R = floatval(6371); // Radius of the earth in km
  $dLat = ($lat2-$lat1) * (M_PI/180); 
  $dLon = ($lon2-$lon1) * (M_PI/180); 
  $a = 
    sin($dLat/2) * sin($dLat/2) +
    cos(($lat1) * (M_PI/180)) * cos(($lat2) * (M_PI/180)) * 
    sin($dLon/2) * sin($dLon/2)
    ; 
  $c = 2 * atan2(sqrt($a), sqrt(1-$a)); 
  $d = $R * $c; // Distance in km
  return $d;
}

try{
	$data = json_decode(file_get_contents('php://input'), true);
	$email = $data["email"];
	$latitude = floatval($data["latitude"]);
	$longitude = floatval($data["longitude"]);

	$table = "friends";

	#Connect to MySql server
	$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

	if ($mysqli->connect_errno) {
	    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
	}

	$res = $mysqli->query("SELECT email, fullName, latestTimestamp, latitude, longitude
		FROM " .$table. " 
		WHERE email <> '$email'
		AND latestTimestamp > TIME(DATE_SUB(NOW(), INTERVAL 1 HOUR))
		ORDER BY email ASC");
	$jsonMainArr = array();
	while ($row = $res->fetch_assoc()) {
		$friendLatitude = floatval($row['latitude']);
		$friendLongitude = floatval($row['longitude']);
		$distance = getDistanceFromLatLonInKm($latitude, $longitude, $friendLatitude, $friendLongitude);
		if ($distance <= 10){
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

} catch (Exception $e) {
	echo 'Caught exception: ',  $e->getMessage(), "\n";
} finally{
	$mysqli->close();
}

?>
