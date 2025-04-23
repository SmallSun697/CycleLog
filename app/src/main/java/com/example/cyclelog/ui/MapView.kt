package com.example.cyclelog.ui

import android.content.Intent
import android.view.animation.LinearInterpolator
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.cyclelog.R
import com.example.cyclelog.data.DarkTheme
import com.example.cyclelog.utils.isScreenTall
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin
import com.mapbox.maps.plugin.animation.CameraAnimatorChangeListener
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants.LOCATION_INDICATOR_LAYER
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions

enum class CameraState {
  POSITION,
  BEARING,
  FREE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapView() {
  val context = LocalContext.current
  val stateViewModel = LocalStateViewModel.current
  val settingsViewModel = LocalSettingsViewModel.current
  val locationViewModel = LocalLocationViewModel.current
  val mapViewModel = LocalMapViewModel.current
  val darkThemeFlow = settingsViewModel.darkTheme
  val locationPoint = locationViewModel.locationPoint
  val pathPoints = locationViewModel.pathPoints
  val cameraState = mapViewModel.cameraState
  val cameraZoom = mapViewModel.cameraZoom
  val currentAzimuth = mapViewModel.currentAzimuth
  val secondary = MaterialTheme.colorScheme.secondary
  val darkTheme = when (darkThemeFlow) {
    DarkTheme.ENABLE -> true
    DarkTheme.DISABLE -> false
    DarkTheme.SYSTEM -> isSystemInDarkTheme()
  }
  val mapViewportState = rememberMapViewportState {
    setCameraOptions {
      center(locationPoint)
      zoom(cameraZoom)
    }
  }

  LaunchedEffect(locationPoint, cameraState) {
    if (cameraState == CameraState.POSITION) {
      mapViewportState.flyTo(
        CameraOptions.Builder()
          .center(locationPoint)
          .zoom(17.0)
          .bearing(0.0)
          .pitch(0.0)
          .build()
      )
    }
  }

  LaunchedEffect(locationPoint, currentAzimuth) {
    if (cameraState == CameraState.BEARING) {
      mapViewportState.easeTo(
        CameraOptions.Builder()
          .center(locationPoint)
          .zoom(17.0)
          .bearing(currentAzimuth)
          .pitch(45.0)
          .build(),
        mapAnimationOptions {
          duration(120L)
          interpolator(LinearInterpolator())
        }
      )
    }
  }

  Scaffold(
    modifier = Modifier
      .navigationBarsPadding(),
    topBar = {
      if (isScreenTall()) {
        CenterAlignedTopAppBar(
          modifier = Modifier
            .fillMaxWidth(),
          title = { Text(Destination.MAP.title) },
          actions = {
            IconButton(
              onClick = {
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
              }
            ) {
              Icon(
                painter = painterResource(R.drawable.rounded_settings_24),
                contentDescription = "setting"
              )
            }
          }
        )
      }
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          mapViewModel.setCameraState(
            if (cameraState == CameraState.POSITION) {
              CameraState.BEARING
            } else {
              CameraState.POSITION
            }
          )
        },
        shape = CircleShape,
        containerColor = if (cameraState != CameraState.FREE) {
          FloatingActionButtonDefaults.containerColor
        } else {
          MaterialTheme.colorScheme.surfaceVariant
        }
      ) {
        when (cameraState) {
          CameraState.POSITION -> {
            Icon(
              painter = painterResource(R.drawable.rounded_my_location_24),
              contentDescription = "目前位置"
            )
          }

          CameraState.BEARING -> {
            Icon(
              painter = painterResource(R.drawable.rounded_explore_24),
              contentDescription = "目前位置及朝向"
            )
          }

          CameraState.FREE -> {
            Icon(
              painter = painterResource(R.drawable.rounded_location_searching_24),
              contentDescription = "自訂位置"
            )
          }
        }
      }
    }
  ) { innerPadding ->
    MapboxMap(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      mapViewportState = mapViewportState,
      style = {
        if (darkTheme) {
          MapStyle(style = "mapbox://styles/smallsun697/cm9fp710w00cp01spbtotetva")
        } else {
          MapStyle(style = Style.STANDARD)
        }
      }
    ) {
      var mapViewRef by remember { mutableStateOf<MapView?>(null) }
      val moveListener = remember {
        object : OnMoveListener {
          override fun onMoveBegin(detector: MoveGestureDetector) {
            mapViewModel.setCameraState(CameraState.FREE)
          }

          override fun onMove(detector: MoveGestureDetector): Boolean = false

          override fun onMoveEnd(detector: MoveGestureDetector) {}
        }
      }
      val zoomChangeListener = remember {
        CameraAnimatorChangeListener<Double> {
          mapViewModel.updateCameraZoom(it)
        }
      }
      val positionListener = remember {
        object : OnIndicatorPositionChangedListener {
          override fun onIndicatorPositionChanged(point: Point) {
            locationViewModel.setLocationPoint(point)

            mapViewRef?.location?.removeOnIndicatorPositionChangedListener(this)
          }
        }
      }

      fun drawRouteLine() {
        mapViewRef!!.mapboxMap.getStyle { style ->
          val sourceId = "route-source"
          val layerId = "route-layer"
          val existingSource = style.getSource(sourceId) as? GeoJsonSource
          val routeFeature = Feature.fromGeometry(LineString.fromLngLats(pathPoints))

          if (existingSource != null) {
            existingSource.feature(routeFeature)
          } else {
            style.addSource(
              geoJsonSource(sourceId) {
                feature(routeFeature)
              }
            )
          }

          if (style.getLayer(layerId) == null) {
            val routeLayer = lineLayer(layerId, sourceId) {
              lineColor(secondary.toArgb())
              lineWidth(10.0)
              lineCap(LineCap.ROUND)
              lineJoin(LineJoin.ROUND)
              lineEmissiveStrength(1.0)
              lineDepthOcclusionFactor(0.6)
            }
            style.addLayerBelow(routeLayer, LOCATION_INDICATOR_LAYER)
          }
        }
      }

      MapEffect(Unit) { mapView ->
        mapViewRef = mapView

        mapView.location.updateSettings {
          enabled = true
          puckBearingEnabled = true
          locationPuck = createDefault2DPuck(withBearing = true)
        }

        mapViewportState.transitionToFollowPuckState(
          followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
            .zoom(17.0)
            .bearing(FollowPuckViewportStateBearing.Constant(0.0))
            .pitch(0.0)
            .build()
        )

        val cameraPlugin = mapView.getPlugin<CameraAnimationsPlugin>(Plugin.MAPBOX_CAMERA_PLUGIN_ID)
        cameraPlugin?.addCameraZoomChangeListener(zoomChangeListener)
        mapView.gestures.addOnMoveListener(moveListener)
        mapView.location.addOnIndicatorPositionChangedListener(positionListener)
        if (stateViewModel.isRecording) drawRouteLine()
      }

      DisposableMapEffect(Unit) { mapView ->
        val cameraPlugin = mapView.getPlugin<CameraAnimationsPlugin>(Plugin.MAPBOX_CAMERA_PLUGIN_ID)

        onDispose {
          mapView.location.updateSettings {
            enabled = false
          }
          mapView.gestures.removeOnMoveListener(moveListener)
          mapView.location.removeOnIndicatorPositionChangedListener(positionListener)
          cameraPlugin?.removeCameraZoomChangeListener(zoomChangeListener)
          mapViewRef = null
        }
      }

      if (stateViewModel.isRecording) {
        MapEffect(pathPoints) {
          drawRouteLine()
        }
      }
    }
  }
}