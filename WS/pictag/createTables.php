
<?php

include "dbinfo.inc";

$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

$createUsers = false;
$createPosts = false;
$createComments = false;
$createUpDownvotes = false;
$createTags = false;
$createPostTags = false;
$createUserTags = true;
$createWaterMarks = false;
$createPurchases = false;
$createParameters = false;


if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}
echo "Created below tables: \n<br/>";

if ($createUsers){
	$table = "users";
	if (!$mysqli->query("DROP TABLE IF EXISTS " . $table)) {
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
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
		profilePicUrl VARCHAR(100),
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
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		post_id INT AUTO_INCREMENT PRIMARY KEY,
		user_id INT,
		image_url VARCHAR(100),
		is_Priced VARCHAR(1),
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
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
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
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
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
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		tag_id INT AUTO_INCREMENT PRIMARY KEY,
		tag VARCHAR(20),
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
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
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
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
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
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
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
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
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
	    die("[ERROR] Failed Dropping the table " . $table . " failed: (" . $mysqli->errno . ") " . $mysqli->error);
	}

	if (!$mysqli->query("CREATE TABLE " . $table . "(
		parameter_id INT AUTO_INCREMENT PRIMARY KEY,
		name VARCHAR(20),
		value VARCHAR(20),
		created_date DATETIME,
		last_updated_date DATETIME
		)")) {
	    die("[ERROR] Failed to create table " . $table . " : (" . $mysqli->errno . ") " . $mysqli->error);
	} else{
		echo $table . "\n<br/>";
	}
}



$mysqli->close();

?>
