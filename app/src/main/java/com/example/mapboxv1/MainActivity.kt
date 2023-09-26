package com.example.mapboxv1

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.WeakReference
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private var longitud:Double=0.0
    private var latitud:Double=0.0

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        latitud=it.latitude()
        longitud=it.longitude()
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        setContentView(mapView)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
            enviarDatosPorTiempo()
        }
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.mapbox_user_icon_shadow,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    class LocationPermissionHelper(val activity: WeakReference<Activity>) {
        private lateinit var permissionsManager: PermissionsManager

        fun checkPermissions(onMapReady: () -> Unit) {
            if (PermissionsManager.areLocationPermissionsGranted(activity.get())) {
                onMapReady()
            } else {
                permissionsManager = PermissionsManager(object : PermissionsListener {
                    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
                        Toast.makeText(
                            activity.get(), "You need to accept location permissions.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onPermissionResult(granted: Boolean) {
                        if (granted) {
                            onMapReady()
                        } else {
                            activity.get()?.finish()
                        }
                    }
                })
                permissionsManager.requestLocationPermissions(activity.get())
            }
        }

        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    //Conexion No remota por laragon
    fun conectarAMySql(): Connection? {
        val seguridad = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(seguridad)
        var connection: Connection? = null
        try {
// Configurar la conexión a la base de datos
            val url = "jdbc:mysql://207.244.255.46/ratiosof74bo_localizador"
            val user = "ratiosof74bo_user_ddt"
            val password = "cek-g~f]w!XV"
// Establecer la conexión
            connection = DriverManager.getConnection(url, user, password)

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return connection
    }
    /*
    private fun insertarDatosADataBase() {
        val connection = conectarAMySql()
        if (connection != null) {
            try {
                // Utilizar las coordenadas latitud y longitud actualizadas
                val fechaHora = fechaActual()

                // Insertar datos en la base de datos
                val query = "INSERT INTO mapGuerrero ( Latitud,  Longitud,  FechaHora) VALUES (?, ?, ?)"
                val preparedStatement = connection.prepareStatement(query)
                preparedStatement.setDouble(1, latitud)
                preparedStatement.setDouble(2, longitud)
                preparedStatement.setString(3, fechaHora)

                preparedStatement.executeUpdate()

                preparedStatement.close()
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
*/
    private fun insertarDatosADataBase() {
        val connection = conectarAMySql()
        if (connection != null) {
            try {
                // Utilizar las coordenadas latitud y longitud actualizadas
                val fechaHora = fechaActual()

                // Utiliza Corrutinas de Kotlin para realizar la operación de base de datos en segundo plano
                CoroutineScope(Dispatchers.IO).launch {
                    // Insertar datos en la base de datos
                    val query = "INSERT INTO mapGuerrero (Latitud, Longitud, FechaHora) VALUES (?, ?, ?)"
                    val preparedStatement = connection.prepareStatement(query)
                    preparedStatement.setDouble(1, latitud)
                    preparedStatement.setDouble(2, longitud)
                    preparedStatement.setString(3, fechaHora)

                    preparedStatement.executeUpdate()

                    preparedStatement.close()
                    connection.close()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
    
    private fun fechaActual(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        val fechaHora = Date()
        return dateFormat.format(fechaHora)
    }
    // Crea un Handler para programar tareas en el hilo principal.
    private val handler = Handler()
    // Función para iniciar la tarea de envío de datos a la base de datos con un retraso de 5 segundos
    private fun enviarDatosPorTiempo() {
        val milisegundos: Long = 5000L // Establece un retraso de 5000 milisegundos = 5 segundos
        // Usando el objeto Handler, se programa una tarea para ejecutarse cada 5 segundos
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Llama a la función para enviar datos a la base de datos
                insertarDatosADataBase()
                // Programa la misma tarea nuevamente después del retraso especificado
                handler.postDelayed(this, milisegundos)
            }
        }, milisegundos)
    }
}
