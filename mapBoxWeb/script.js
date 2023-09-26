fetch('conexion.php')
    .then(response => response.json())
    .then(data => {
        if (data && data.latitud && data.longitud) {
            // Actualizar la ubicación del marcador en el mapa
            marker.setLngLat([data.longitud, data.latitud]);
            // Centrar el mapa en la nueva ubicación
            map.setCenter([data.longitud, data.latitud]);
        } else {
            console.log("No se encontraron coordenadas.");
        }
    })
    .catch(error => {
        console.error("Error al obtener coordenadas:", error);
    });
setInterval(updateLocation, 5000);