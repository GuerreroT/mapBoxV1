<?php
// Conexión a la base de datos
$servername = "207.244.255.46";
$username = "ratiosof74bo_user_ddt";
$password = "cek-g~f]w!XV";
$dbname = "ratiosof74bo_localizador";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Error de conexión: " . $conn->connect_error);
}

// Consulta SQL para obtener la última ubicación
$sql = "SELECT latitud, longitud FROM mapGuerrero ORDER BY fechaHora DESC LIMIT 1";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    // Convertir el resultado en formato JSON
    $row = $result->fetch_assoc();
    $location = array("longitud" => $row["longitud"], "latitud" => $row["latitud"]);
    echo json_encode($location);
} else {
    echo json_encode(array("error" => "No se encontraron datos de ubicación en la base de datos."));
}

$conn->close();
?>