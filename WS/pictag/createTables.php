
<?php

include "dbinfo.inc";

$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

$createUsers = false;
$createPosts = false;
$createComments = false;
$createUpDownvotes = false;
$createTags = false;
$createPostTags = false;
$createUserTags = false;
$createWaterMarks = false;
$createPurchases = false;
$createParameters = false;
$insertParameters = false;


if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}
echo "Created below tables: \n<br/>";

if ($createUsers){
	$table = "users";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		user_id INT AUTO_INCREMENT PRIMARY KEY,
		email VARCHAR(20),
		firstName VARCHAR(20),
		lastName VARCHAR(20),
		fb_profile_id VARCHAR(20),
		dob VARCHAR(10),
		gender VARCHAR(10),
		created_date DATETIME,
		last_updated_date DATETIME,
		last_login_time DATETIME,
		reputation INT,
		profilePicUrl VARCHAR(1024),
		lastAlertedTime DATETIME,
		token varchar(1024)
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($createPosts){
	$table = "posts";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		post_id INT AUTO_INCREMENT PRIMARY KEY,
		user_id INT,
		image_url VARCHAR(1024),
		is_Priced VARCHAR(1),
		price DECIMAL(10,2),
		description VARCHAR(100),
		created_date DATETIME,
		last_updated_date DATETIME,
		status VARCHAR(1),
		is_private VARCHAR(1),
		watermark_id INT,
		category INT,
		up_count INT,
		down_count INT
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($createComments){
	$table = "comments";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		comment_id INT AUTO_INCREMENT PRIMARY KEY,
		post_id INT,
		user_id INT,
		comment VARCHAR(100),
		created_date DATETIME,
		last_updated_date DATETIME,
		deleted_date DATETIME,
		status VARCHAR(1)
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($createUpDownvotes){
	$table = "updownvotes";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		ud_id INT AUTO_INCREMENT PRIMARY KEY,
		post_id INT,
		user_id INT,
		ud_flag VARCHAR(1),
		created_date DATETIME
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($createTags){
	$table = "tags";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		tag_id INT AUTO_INCREMENT PRIMARY KEY,
		tag VARCHAR(50),
		created_date DATETIME,
		last_updated_date DATETIME,
		last_alerted_date DATETIME
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($createPostTags){
	$table = "post_tags";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		post_id INT,
		tag_id INT,
		type VARCHAR(1)
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($createUserTags){
	$table = "user_tags";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		user_id INT,
		tag_id INT,
		notify VARCHAR(1),
		minUpVotes INT,
		created_date DATETIME
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($createWaterMarks){
	$table = "watermarks";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		watermark_id INT AUTO_INCREMENT PRIMARY KEY,
		source VARCHAR(100),
		created_date DATETIME,
		last_updated_date DATETIME,
		text VARCHAR(20),
		user_id INT,
		font_family VARCHAR(20),
		text_size INT,
		text_color VARCHAR(10),
		ti_flag VARCHAR(1)
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($createPurchases){
	$table = "purchases";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		pruchase_id INT AUTO_INCREMENT PRIMARY KEY,
		post_id INT,
		user_id INT,
		price DECIMAL(16,2),
		payment_method VARCHAR(20),
		created_date DATETIME,
		isRefunded VARCHAR(1)
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($createParameters){
	$table = "parameters";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		parameter_id INT AUTO_INCREMENT PRIMARY KEY,
		name VARCHAR(50),
		value VARCHAR(50),
		group_name VARCHAR(20),
		created_date DATETIME,
		last_updated_date DATETIME
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}

if ($insertParameters){
	$table = "parameters";
	if (!$mysqli->query("DELETE FROM " . $table)) {
		echo $mysqli->error;
	    die("[ERROR] Deleting the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	$categories = array("Abstract photography","ActionShot","Aerial photography","Analog photography","Architectural photography","Astrophotography","Aviation photography","Banquet photography","Burns Archive","Candid photography","Close-up","Cloudscape photography","Conceptual photography","Concert photography","Conservation photography","Documentary photography","Event photography","Fancy portrait","Fashion photography","Fauxtography","Femto-photography","Film still","Fine-art photography","Fire photography","Fireworks photography","Food photography","Forensic photography","Genre art","Geophotography","Glamour photography","High key","High-speed photography","Humanist photography","Imagery intelligence","In-game photography","International Society for Aviation Photography","Kirlian photography","Lifestyle photography","Lo-fi photography","Lomography","Long-exposure photography","Low key","Macro photography","Medical photography","Monochrome photography","Narrative photography","Night photography","Old-time photography","Panorama","Panoramic photography","Pellier Noir","Photo op","Photobiography","Photography by indigenous peoples of the Americas","Photojournalism","Photovoice","Photowalking","Pictorialism","Polaroid art","Portrait photography","Post-mortem photography","Red shirt (photography)","Satellite imagery","Secret photography","Slow photography","Snapshot (photography)","Snapshot aesthetic","Social documentary photography","Social photography","Soft focus","Star trail","Still life photography","Still photography","Stock photography","Straight photography","Street photography","Subminiature photography","Tele-snaps","The Straight Up","Thoughtography","Time-lapse photography","Travel photography","Ultraviolet photography","Underwater videography","Vernacular photography","Vintage print","Visual anthropology");

	$prefix = "INSERT INTO parameters (name, value, group_name, created_date, last_updated_date) VALUES ('";
	$suffix = "', 'CATEGORY', now(), now())";
	$num = 1;
	foreach ($categories as $category){
		$query = $prefix . $num . "', '" . $mysqli->real_escape_string($category) . $suffix;
		echo $query . "<br/>";
		if (!$mysqli->query($query)) {
			echo "[ERROR] Failed to insert into " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error;
		    die("[ERROR] Failed to insert into " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
		}
		$num = $num + 1;
	}

	$watermarks = array("General", "Advanced");

	$prefix = "INSERT INTO parameters (name, value, group_name, created_date, last_updated_date) VALUES ('";
	$suffix = "', 'WATERMARK', now(), now())";
	$num = 1;
	foreach ($watermarks as $watermark){
		$query = $prefix . $num . "', '" . $mysqli->real_escape_string($watermark) . $suffix;
		echo $query . "<br/>";
		if (!$mysqli->query($query)) {
			echo "[ERROR] Failed to insert into " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error;
		    die("[ERROR] Failed to insert into " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
		}
		$num = $num + 1;
	}
	echo "Insertion of parameters successful!!";
}



$mysqli->close();

?>
