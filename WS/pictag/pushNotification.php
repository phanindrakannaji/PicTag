
<?php

include "dbinfo.inc";

define('API_ACCESS_KEY', 'AAAA-i04Bis:APA91bFGeCi7umkTQveON03UFznMMq78SV_HjqkXUhf3p165c1g6JwmwuM_CY2jSGDjwBOkjishvorlAxvx0ZSW5MqI_Lg02834cpq9CLwQM_2cI_W8YwLZYOTDzq3vpF8SKt6l6fJ6K');
$table = "friends";

class Friend
{
    // property declaration
    public $email;
    public $fullName;
    public $latestTimestamp;
    public $lastAlertedTime;
    public $latitude;
    public $longitude;
    public $token;

    public function __construct($pEmail, $pFullName, $pLatestTimestamp, $pLastAlertedTime, $pLatitude, $pLongitude, $pToken)
  	{
    	$this->email = $pEmail;
    	$this->fullName = $pFullName;
    	$this->latestTimestamp = $pLatestTimestamp;
    	$this->lastAlertedTime = $pLastAlertedTime;
    	$this->latitude = $pLatitude;
    	$this->longitude = $pLongitude;
    	$this->token = $pToken;
  	}

  	public function getEmail(){
  		return $this->email;
  	}
  	public function getFullName(){
  		return $this->fullName;
  	}
  	public function getLatestTimeStamp(){
  		return $this->latestTimestamp;
  	}
  	public function getLastAlertedTime(){
  		return $this->lastAlertedTime;
  	}
  	public function getLatitude(){
  		return $this->latitude;
  	}
  	public function getLongitude(){
  		return $this->longitude;
  	}
  	public function getToken(){
  		return $this->token;
  	}
}

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

$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);
if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}

$users = array();

// select the users whose last active time is greater than 1 hour and the last alerted time is greater than 3 hours
$res = $mysqli->query("SELECT email, fullName, latestTimestamp, lastAlertedTime, latitude, longitude, token  
	FROM " .$table. 
	" WHERE 
	 latestTimestamp < TIME(DATE_SUB(NOW(), INTERVAL 1 HOUR))
	 AND lastAlertedTime < TIME(DATE_SUB(NOW(), INTERVAL 3 HOUR))
	 ORDER BY email ASC");
while ($row = $res->fetch_assoc()) {
	$fuser = new Friend($row['email'], $row['fullName'], $row['latestTimestamp'],$row['lastAlertedTime'],$row['latitude'],$row['longitude'], $row['token']);
    $users[] = $fuser;
    echo "Processing friends list for " . $fuser->getFullName() . "\n";
}

// send push notification about the active users to each inactive user
foreach ($users as $user){
	$email = $mysqli->real_escape_string($user->getEmail());
	$token = $user->getToken();
	$latitude = $user->getLatitude();
	$longitude = $user->getLongitude();
	echo "latitude: " . $latitude;
	echo "longitude: " . $longitude;
	$registrationIds = array();
	$registrationIds[] = $token;

	$friends = array();
	$fres = $mysqli->query("SELECT email, fullName, latestTimestamp, lastAlertedTime, latitude, longitude, token
	FROM " .$table. 
	" WHERE latestTimestamp > TIME(DATE_SUB(NOW(), INTERVAL 1 HOUR))
	  AND email <> '$email'
	  ORDER BY latestTimestamp DESC");
	while ($row = $fres->fetch_assoc()) {
	    $friendLatitude = floatval($row['latitude']);
		$friendLongitude = floatval($row['longitude']);
		echo "friendLatitude: " . $friendLatitude;
		echo "friendLongitude: " . $friendLongitude;
		$distance = getDistanceFromLatLonInKm($latitude, $longitude, $friendLatitude, $friendLongitude);
		echo "distance: " . $distance;
		if ($distance <= 10){
			$friend = new Friend($row['email'], $row['fullName'], $row['latestTimestamp'],$row['lastAlertedTime'],$row['latitude'],$row['longitude'], $row['token']);
			$friends[] = $friend;
		}
	}
	echo "Found " . count($friends) . " friends nearby to " . $email . ". \n";
	if (count($friends) > 0){
		echo "Sending notification to '$email', message: \n";
		$message = "";
		if (count($friends) <=3){
			foreach($friends as $friend){
				$friendFullName = $friend->getFullName();
				$message = $message . $friendFullName . " ";
			}
		} else{
			$friendFullName = $friends[0]->getFullName();
			$numFriends = count($friends);
			$numOthers = $numFriends - 1;
			$message = $message . $friendFullName . " and $numOthers other friends ";
		}
		if (count($friends) == 1){
			$message = $message . "is nearby! Tap to locate on map.";
		} else{
			$message = $message . "are nearby! Tap to locate them on map.";
		}
		echo "\t" . $message . "\n";
		$msg = array
		(
			'message' 	=> $message,
			'title'		=> 'FriendFinder Alert',
			'subtitle'	=> ''
		);
		$fields = array
		(
			'registration_ids' 	=> $registrationIds,
			'data'			=> $msg
		);
		 
		$headers = array
		(
			'Authorization: key=' . API_ACCESS_KEY,
			'Content-Type: application/json'
		);

		$ch = curl_init();
		curl_setopt( $ch,CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send' );
		curl_setopt( $ch,CURLOPT_POST, true );
		curl_setopt( $ch,CURLOPT_HTTPHEADER, $headers );
		curl_setopt( $ch,CURLOPT_RETURNTRANSFER, true );
		curl_setopt( $ch,CURLOPT_SSL_VERIFYPEER, false );
		curl_setopt( $ch,CURLOPT_POSTFIELDS, json_encode( $fields ) );
		$result = curl_exec($ch );
		echo $result;
		curl_close( $ch );

		$ures = $mysqli->query("UPDATE friends SET lastAlertedTime = now() WHERE email = '$email'");

	}
}
    

$mysqli->close();
?>
