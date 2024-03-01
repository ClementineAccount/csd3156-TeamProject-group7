/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.core.examples.kotlin.helloar

import android.media.MediaPlayer
import android.opengl.GLES30
import android.opengl.Matrix
import android.os.Handler
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.csd3156.team7.FarmItem
import com.csd3156.team7.ShopItem
import com.google.ar.core.Anchor
import com.google.ar.core.Camera
import com.google.ar.core.DepthPoint
import com.google.ar.core.Frame
import com.google.ar.core.GeospatialPose
import com.google.ar.core.InstantPlacementPoint
import com.google.ar.core.LightEstimate
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.Trackable
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper
import com.google.ar.core.examples.java.common.helpers.TrackingStateHelper
import com.google.ar.core.examples.java.common.samplerender.Framebuffer
import com.google.ar.core.examples.java.common.samplerender.GLError
import com.google.ar.core.examples.java.common.samplerender.Mesh
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.core.examples.java.common.samplerender.Shader
import com.google.ar.core.examples.java.common.samplerender.Texture
import com.google.ar.core.examples.java.common.samplerender.VertexBuffer
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer
import com.google.ar.core.examples.java.common.samplerender.arcore.PlaneRenderer
import com.google.ar.core.examples.java.common.samplerender.arcore.SpecularCubemapFilter
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.NotYetAvailableException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Random
import kotlin.math.sqrt

import android.widget.Toast
import com.csd3156.team7.SoundEffectsManager

// Create this with the anchor stuff
// TODO: Also add the shape here that it will represent
private data class CollectableObject(
  val x : Float,
  val y : Float,
  val z:  Float,
  val radius: Float = 1.0f //For distance hit point check with the tap pointer
)

/** Renders the HelloAR application using our example Renderer. */
class HelloArRenderer(val activity: HelloArActivity, private val listener: TapInterface) :
  SampleRender.Renderer, DefaultLifecycleObserver {
  companion object {
    val TAG = "HelloArRenderer"

    // See the definition of updateSphericalHarmonicsCoefficients for an explanation of these
    // constants.
    private val sphericalHarmonicFactors =
      floatArrayOf(
        0.282095f,
        -0.325735f,
        0.325735f,
        -0.325735f,
        0.273137f,
        -0.273137f,
        0.078848f,
        -0.273137f,
        0.136569f
      )

    private val Z_NEAR = 0.1f
    private val Z_FAR = 100f

    // Assumed distance from the device camera to the surface on which user will try to place
    // objects.
    // This value affects the apparent scale of objects while the tracking method of the
    // Instant Placement point is SCREENSPACE_WITH_APPROXIMATE_DISTANCE.
    // Values in the [0.2, 2.0] meter range are a good choice for most AR experiences. Use lower
    // values for AR experiences where users are expected to place objects on surfaces close to the
    // camera. Use larger values for experiences where the user will likely be standing and trying
    // to
    // place an object on the ground or floor in front of them.
    val APPROXIMATE_DISTANCE_METERS = 2.0f

    val CUBEMAP_RESOLUTION = 16
    val CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES = 32
  }

  lateinit var render: SampleRender
  lateinit var planeRenderer: PlaneRenderer
  lateinit var backgroundRenderer: BackgroundRenderer
  lateinit var virtualSceneFramebuffer: Framebuffer
  var hasSetTextureNames = false

  // Point Cloud
  lateinit var pointCloudVertexBuffer: VertexBuffer
  lateinit var pointCloudMesh: Mesh
  lateinit var pointCloudShader: Shader

  // Keep track of the last point cloud rendered to avoid updating the VBO if point cloud
  // was not changed.  Do this using the timestamp since we can't compare PointCloud objects.
  var lastPointCloudTimestamp: Long = 0

  // Virtual object (ARCore pawn)
  lateinit var virtualObjectMesh: Mesh
  lateinit var virtualObjectMeshCube : Mesh
  lateinit var virtualObjectMeshPyramid : Mesh
  lateinit var virtualObjectMeshSphere: Mesh

  lateinit var virtualObjectShader: Shader
  lateinit var virtualObjectAlbedoTexture: Texture
  lateinit var virtualObjectAlbedoInstantPlacementTexture: Texture

  lateinit var virtualObjectCollectableShader : Shader
  lateinit var geospatialAnchorVirtualObjectShader: Shader

  private val wrappedAnchors = mutableListOf<WrappedAnchor>()
  private val gpsAnchors = mutableListOf<Anchor>()

  private var mediaPlayer: MediaPlayer? = null

  private var soundEffectsManager: SoundEffectsManager = SoundEffectsManager(activity.applicationContext)
  private val audioResources = arrayOf(
    R.raw.drop1,
    R.raw.drop2,
    R.raw.drop3,
    R.raw.drop4,
    R.raw.drop5
  )

  // Environmental HDR
  lateinit var dfgTexture: Texture
  lateinit var cubemapFilter: SpecularCubemapFilter

  // Temporary matrix allocated here to reduce number of allocations for each frame.
  val modelMatrix = FloatArray(16)
  val viewMatrix = FloatArray(16)
  val projectionMatrix = FloatArray(16)
  val modelViewMatrix = FloatArray(16) // view x model

  val modelViewProjectionMatrix = FloatArray(16) // projection x view x model

  val sphericalHarmonicsCoefficients = FloatArray(9 * 3)
  val viewInverseMatrix = FloatArray(16)
  val worldLightDirection = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
  val viewLightDirection = FloatArray(4) // view x world light direction

  //Store the collcetable objects that are generated
  private var collectableList: MutableList<CollectableObject> = mutableListOf()



  val session
    get() = activity.arCoreSessionHelper.session

  val displayRotationHelper = DisplayRotationHelper(activity)
  val trackingStateHelper = TrackingStateHelper(activity)

  override fun onResume(owner: LifecycleOwner) {
    displayRotationHelper.onResume()
    hasSetTextureNames = false
  }

  override fun onPause(owner: LifecycleOwner) {
    displayRotationHelper.onPause()
  }

  override fun onSurfaceCreated(render: SampleRender) {
    // Prepare the rendering objects.
    // This involves reading shaders and 3D model files, so may throw an IOException.
    try {
      planeRenderer = PlaneRenderer(render)
      backgroundRenderer = BackgroundRenderer(render)
      virtualSceneFramebuffer = Framebuffer(render, /*width=*/ 1, /*height=*/ 1)

      cubemapFilter =
        SpecularCubemapFilter(render, CUBEMAP_RESOLUTION, CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES)
      // Load environmental lighting values lookup table
      dfgTexture =
        Texture(
          render,
          Texture.Target.TEXTURE_2D,
          Texture.WrapMode.CLAMP_TO_EDGE,
          /*useMipmaps=*/ false
        )
      // The dfg.raw file is a raw half-float texture with two channels.
      val dfgResolution = 64
      val dfgChannels = 2
      val halfFloatSize = 2

      val buffer: ByteBuffer =
        ByteBuffer.allocateDirect(dfgResolution * dfgResolution * dfgChannels * halfFloatSize)
      activity.assets.open("models/dfg.raw").use { it.read(buffer.array()) }

      // SampleRender abstraction leaks here.
      GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, dfgTexture.textureId)
      GLError.maybeThrowGLException("Failed to bind DFG texture", "glBindTexture")
      GLES30.glTexImage2D(
        GLES30.GL_TEXTURE_2D,
        /*level=*/ 0,
        GLES30.GL_RG16F,
        /*width=*/ dfgResolution,
        /*height=*/ dfgResolution,
        /*border=*/ 0,
        GLES30.GL_RG,
        GLES30.GL_HALF_FLOAT,
        buffer
      )
      GLError.maybeThrowGLException("Failed to populate DFG texture", "glTexImage2D")

      // Point cloud
      pointCloudShader =
        Shader.createFromAssets(
            render,
            "shaders/point_cloud.vert",
            "shaders/point_cloud.frag",
            /*defines=*/ null
          )
          .setVec4("u_Color", floatArrayOf(31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f))
          .setFloat("u_PointSize", 5.0f)

      // four entries per vertex: X, Y, Z, confidence
      pointCloudVertexBuffer =
        VertexBuffer(render, /*numberOfEntriesPerVertex=*/ 4, /*entries=*/ null)
      val pointCloudVertexBuffers = arrayOf(pointCloudVertexBuffer)
      pointCloudMesh =
        Mesh(render, Mesh.PrimitiveMode.POINTS, /*indexBuffer=*/ null, pointCloudVertexBuffers)

      // Virtual object to render (ARCore pawn)
      virtualObjectAlbedoTexture =
        Texture.createFromAsset(
          render,
          "models/TestCube.png",
          Texture.WrapMode.CLAMP_TO_EDGE,
          Texture.ColorFormat.SRGB
        )

     /* virtualObjectAlbedoInstantPlacementTexture =
        Texture.createFromAsset(
          render,
          "models/pawn_albedo_instant_placement.png",
          Texture.WrapMode.CLAMP_TO_EDGE,
          Texture.ColorFormat.SRGB
        )

      val virtualObjectPbrTexture =
        Texture.createFromAsset(
          render,
          "models/pawn_roughness_metallic_ao.png",
          Texture.WrapMode.CLAMP_TO_EDGE,
          Texture.ColorFormat.LINEAR
        )*/
      virtualObjectMesh = Mesh.createFromAsset(render, "models/TestCube.obj")
      virtualObjectMeshCube = Mesh.createFromAsset(render, "models/TestCube.obj")
      virtualObjectMeshPyramid = Mesh.createFromAsset(render, "models/TestTriangle.obj")
      virtualObjectMeshSphere = Mesh.createFromAsset(render, "models/TestSphere.obj")
      virtualObjectShader =
        Shader.createFromAssets(
            render,
            "shaders/environmental_hdr.vert",
            "shaders/environmental_hdr.frag",
            mapOf("NUMBER_OF_MIPMAP_LEVELS" to cubemapFilter.numberOfMipmapLevels.toString())
          )
          .setTexture("u_AlbedoTexture", virtualObjectAlbedoTexture)
         // .setTexture("u_RoughnessMetallicAmbientOcclusionTexture", virtualObjectPbrTexture)
          .setTexture("u_Cubemap", cubemapFilter.filteredCubemapTexture)
          .setTexture("u_DfgTexture", dfgTexture)

      virtualObjectCollectableShader = Shader.createFromAssets(
        render,
        "shaders/ar_unlit_object.vert",
        "shaders/ar_unlit_color_object.frag",  /* defines= */
        null
      )
        .setTexture("u_Texture", virtualObjectAlbedoTexture)


      geospatialAnchorVirtualObjectShader = Shader.createFromAssets(
        render,
        "shaders/ar_unlit_object.vert",
        "shaders/ar_unlit_object.frag",  /* defines= */
        null
      )
        .setTexture("u_Texture", virtualObjectAlbedoTexture)



    } catch (e: IOException) {
      Log.e(TAG, "Failed to read a required asset file", e)
      showError("Failed to read a required asset file: $e")
    }
  }

  override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
    displayRotationHelper.onSurfaceChanged(width, height)
    virtualSceneFramebuffer.resize(width, height)
  }

  private fun getScale(anchorPose: Pose, cameraPose: Pose): Float {
    val distance = Math.sqrt(
      Math.pow((anchorPose.tx() - cameraPose.tx()).toDouble(), 2.0)
              + Math.pow((anchorPose.ty() - cameraPose.ty()).toDouble(), 2.0)
              + Math.pow((anchorPose.tz() - cameraPose.tz()).toDouble(), 2.0)
    )
    val mapDistance = Math.min(Math.max(2.0, distance), 20.0)
    return (mapDistance - 2).toFloat() / (20 - 2) + 1
  }


  override fun onDrawFrame(render: SampleRender) {
    val session = session ?: return

    // Texture names should only be set once on a GL thread unless they change. This is done during
    // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
    // initialized during the execution of onSurfaceCreated.
    if (!hasSetTextureNames) {
      session.setCameraTextureNames(intArrayOf(backgroundRenderer.cameraColorTexture.textureId))
      hasSetTextureNames = true
    }

    // -- Update per-frame state

    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session)

    // Obtain the current frame from ARSession. When the configuration is set to
    // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
    // camera framerate.
    val frame =
      try {
        session.update()
      } catch (e: CameraNotAvailableException) {
        Log.e(TAG, "Camera not available during onDrawFrame", e)
        showError("Camera not available. Try restarting the app.")
        return
      }

    val camera = frame.camera

    // Update BackgroundRenderer state to match the depth settings.
    try {
      backgroundRenderer.setUseDepthVisualization(
        render,
        activity.depthSettings.depthColorVisualizationEnabled()
      )
      backgroundRenderer.setUseOcclusion(render, activity.depthSettings.useDepthForOcclusion())
    } catch (e: IOException) {
      Log.e(TAG, "Failed to read a required asset file", e)
      showError("Failed to read a required asset file: $e")
      return
    }

    // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
    // used to draw the background camera image.
    backgroundRenderer.updateDisplayGeometry(frame)
    val shouldGetDepthImage =
      activity.depthSettings.useDepthForOcclusion() ||
        activity.depthSettings.depthColorVisualizationEnabled()
    if (camera.trackingState == TrackingState.TRACKING && shouldGetDepthImage) {
      try {
        val depthImage = frame.acquireDepthImage16Bits()
        backgroundRenderer.updateCameraDepthTexture(depthImage)
        depthImage.close()
      } catch (e: NotYetAvailableException) {
        // This normally means that depth data is not available yet. This is normal so we will not
        // spam the logcat with this.
      }
    }

    // Handle one tap per frame.
    handleTap(frame, camera)

    // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
    trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)

    // Show a message based on whether tracking has failed, if planes are detected, and if the user
    // has placed any objects.
    val message: String? =
      when {
        camera.trackingState == TrackingState.PAUSED &&
          camera.trackingFailureReason == TrackingFailureReason.NONE ->
          activity.getString(R.string.searching_planes)
        camera.trackingState == TrackingState.PAUSED ->
          TrackingStateHelper.getTrackingFailureReasonString(camera)
        session.hasTrackingPlane() && wrappedAnchors.isEmpty() && activity.startCollecting ->
          activity.getString(R.string.waiting_taps)
        session.hasTrackingPlane() && wrappedAnchors.isNotEmpty() -> null
        else -> activity.getString(R.string.searching_planes)
      }
    if (message == null) {
      activity.view.snackbarHelper.hide(activity)
    } else {
      activity.view.snackbarHelper.showMessage(activity, message)
    }

    // -- Draw background
    if (frame.timestamp != 0L) {
      // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
      // drawing possible leftover data from previous sessions if the texture is reused.
      backgroundRenderer.drawBackground(render)
    }

    // If not tracking, don't draw 3D objects.
    if (camera.trackingState == TrackingState.PAUSED) {
      return
    }

    // -- Draw non-occluded virtual objects (planes, point cloud)

    // Get projection matrix.
    camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR)

    // Get camera matrix and draw.
    camera.getViewMatrix(viewMatrix, 0)
    frame.acquirePointCloud().use { pointCloud ->
      if (pointCloud.timestamp > lastPointCloudTimestamp) {
        pointCloudVertexBuffer.set(pointCloud.points)
        lastPointCloudTimestamp = pointCloud.timestamp
      }
      Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
      pointCloudShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
      render.draw(pointCloudMesh, pointCloudShader)
    }

    // Visualize planes.
    planeRenderer.drawPlanes(
      render,
      session.getAllTrackables<Plane>(Plane::class.java),
      camera.displayOrientedPose,
      projectionMatrix
    )

    // -- Draw occluded virtual objects

    // Update lighting parameters in the shader
    updateLightEstimation(frame.lightEstimate, viewMatrix)

    // Visualize anchors created by touch.
    render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f)
    synchronized(wrappedAnchors)
    {
      for ((anchor, trackable) in
      wrappedAnchors.filter { it.anchor.trackingState == TrackingState.TRACKING }) {

        //Cannot just put in another function got issues
        //renderCollectables(anchor, trackable)

        //Causes crash sometimes I think due to concurrency modifying collectableList during
        //rendering operation....

        //easy fix since N is small is to clone the list
        //var collectableListClone = collectableList.toMutableList()
        synchronized(collectableList)
        {
          for (collectable in collectableList) {
            anchor.pose.toMatrix(modelMatrix, 0)

            Matrix.translateM(modelMatrix, 0, collectable.x, collectable.y, collectable.z)

            // Calculate model/view/projection matrices
            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

            // Update shader properties and draw
            //virtualObjectCollectableShader.setMat4("u_ModelView", modelViewMatrix)
            virtualObjectCollectableShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
            val texture =
              if ((trackable as? InstantPlacementPoint)?.trackingMethod ==
                InstantPlacementPoint.TrackingMethod.SCREENSPACE_WITH_APPROXIMATE_DISTANCE
              ) {
                virtualObjectAlbedoInstantPlacementTexture
              } else {
                virtualObjectAlbedoTexture
              }
            //virtualObjectCollectableShader.setTexture("u_Texture", texture)

            // Testing it
            virtualObjectCollectableShader.setVec3("u_color",
              floatArrayOf(activity.currentShapeColor.first.toFloat() / 255.0f,
                activity.currentShapeColor.second.toFloat() / 255.0f,
                  activity.currentShapeColor.third.toFloat() / 255.0f))

            if (activity.currentShapeFarm == "Cube")
            {
              render.draw(virtualObjectMeshCube, virtualObjectCollectableShader, virtualSceneFramebuffer)
            }
            else if (activity.currentShapeFarm == "Pyramid")
            {
              render.draw(virtualObjectMeshPyramid, virtualObjectCollectableShader, virtualSceneFramebuffer)
            }
            else if (activity.currentShapeFarm == "Sphere")
            {
              render.draw(virtualObjectMeshSphere, virtualObjectCollectableShader, virtualSceneFramebuffer)
            }
            else
            {
              // Fallback
              render.draw(virtualObjectMesh, virtualObjectCollectableShader, virtualSceneFramebuffer)
            }

            //render.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer)
          }
        }


        // Get the current pose of an Anchor in world space. The Anchor pose is updated
        // during calls to session.update() as ARCore refines its estimate of the world.

        //Objects are relative to the (one) anchor


        //Don't render the anchor itself.

//      anchor.pose.toMatrix(modelMatrix, 0)
//      Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, 0.0f)
//
//      // Calculate model/view/projection matrices
//      Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
//      Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)
//
//      // Update shader properties and draw
//      virtualObjectShader.setMat4("u_ModelView", modelViewMatrix)
//      virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
//      val texture =
//        if ((trackable as? InstantPlacementPoint)?.trackingMethod ==
//            InstantPlacementPoint.TrackingMethod.SCREENSPACE_WITH_APPROXIMATE_DISTANCE
//        ) {
//          virtualObjectAlbedoInstantPlacementTexture
//        } else {
//          virtualObjectAlbedoTexture
//        }
//      virtualObjectShader.setTexture("u_AlbedoTexture", texture)
//      render.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer)
      }


      // Visualize anchors created by GPS
      // I copy from geospatial_java from the samples
      /*    for (anchor in gpsAnchors) {
        // Get the current pose of an Anchor in world space. The Anchor pose is updated
              // during calls to session.update() as ARCore refines its estimate of the world.
              // Only render resolved Terrain & Rooftop anchors and Geospatial anchors.

              // Get the current pose of an Anchor in world space. The Anchor pose is updated
              // during calls to session.update() as ARCore refines its estimate of the world.
              // Only render resolved Terrain & Rooftop anchors and Geospatial anchors.
            if (anchor.trackingState != TrackingState.TRACKING) {
              continue
            }
            anchor.pose.toMatrix(modelMatrix, 0)
            val scaleMatrix = FloatArray(16)
            Matrix.setIdentityM(scaleMatrix, 0)
            val scale: Float = getScale(anchor.pose, camera.displayOrientedPose)
            scaleMatrix[0] = scale
            scaleMatrix[5] = scale
            scaleMatrix[10] = scale
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, scaleMatrix, 0)
            // Rotate the virtual object 180 degrees around the Y axis to make the object face the GL
            // camera -Z axis, since camera Z axis faces toward users.
            // Rotate the virtual object 180 degrees around the Y axis to make the object face the GL
            // camera -Z axis, since camera Z axis faces toward users.
            val rotationMatrix = FloatArray(16)
            Matrix.setRotateM(rotationMatrix, 0, 180f, 0.0f, 1.0f, 0.0f)
            val rotationModelMatrix = FloatArray(16)
            Matrix.multiplyMM(rotationModelMatrix, 0, modelMatrix, 0, rotationMatrix, 0)
            // Calculate model/view/projection matrices
            // Calculate model/view/projection matrices
            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, rotationModelMatrix, 0)
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

            geospatialAnchorVirtualObjectShader.setMat4(
              "u_ModelViewProjection", modelViewProjectionMatrix
            )
            render.draw(
              virtualObjectMesh, geospatialAnchorVirtualObjectShader, virtualSceneFramebuffer
            )
          }*/

      // Compose the virtual scene with the background.
      backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR)
      }
    }


  /** Checks if we detected at least one plane. */
  private fun Session.hasTrackingPlane() =
    getAllTrackables(Plane::class.java).any { it.trackingState == TrackingState.TRACKING }

  /** Update state based on the current frame's light estimation. */
  private fun updateLightEstimation(lightEstimate: LightEstimate, viewMatrix: FloatArray) {
    if (lightEstimate.state != LightEstimate.State.VALID) {
      virtualObjectShader.setBool("u_LightEstimateIsValid", false)
      return
    }
    virtualObjectShader.setBool("u_LightEstimateIsValid", true)
    Matrix.invertM(viewInverseMatrix, 0, viewMatrix, 0)
    virtualObjectShader.setMat4("u_ViewInverse", viewInverseMatrix)
    updateMainLight(
      lightEstimate.environmentalHdrMainLightDirection,
      lightEstimate.environmentalHdrMainLightIntensity,
      viewMatrix
    )
    updateSphericalHarmonicsCoefficients(lightEstimate.environmentalHdrAmbientSphericalHarmonics)
    cubemapFilter.update(lightEstimate.acquireEnvironmentalHdrCubeMap())
  }

  private fun updateMainLight(
    direction: FloatArray,
    intensity: FloatArray,
    viewMatrix: FloatArray
  ) {
    // We need the direction in a vec4 with 0.0 as the final component to transform it to view space
    worldLightDirection[0] = direction[0]
    worldLightDirection[1] = direction[1]
    worldLightDirection[2] = direction[2]
    Matrix.multiplyMV(viewLightDirection, 0, viewMatrix, 0, worldLightDirection, 0)
    virtualObjectShader.setVec4("u_ViewLightDirection", viewLightDirection)
    virtualObjectShader.setVec3("u_LightIntensity", intensity)
  }

  private fun updateSphericalHarmonicsCoefficients(coefficients: FloatArray) {
    // Pre-multiply the spherical harmonics coefficients before passing them to the shader. The
    // constants in sphericalHarmonicFactors were derived from three terms:
    //
    // 1. The normalized spherical harmonics basis functions (y_lm)
    //
    // 2. The lambertian diffuse BRDF factor (1/pi)
    //
    // 3. A <cos> convolution. This is done to so that the resulting function outputs the irradiance
    // of all incoming light over a hemisphere for a given surface normal, which is what the shader
    // (environmental_hdr.frag) expects.
    //
    // You can read more details about the math here:
    // https://google.github.io/filament/Filament.html#annex/sphericalharmonics
    require(coefficients.size == 9 * 3) {
      "The given coefficients array must be of length 27 (3 components per 9 coefficients"
    }

    // Apply each factor to every component of each coefficient
    for (i in 0 until 9 * 3) {
      sphericalHarmonicsCoefficients[i] = coefficients[i] * sphericalHarmonicFactors[i / 3]
    }
    virtualObjectShader.setVec3Array(
      "u_SphericalHarmonicsCoefficients",
      sphericalHarmonicsCoefficients
    )
  }


  //DONT USE THIS FUNCTION ITS BROKEN
  private fun renderCollectables(anchor: Anchor, trackable: Trackable)
  {
    //val collectableListCloned = collectableList.toMutableList()
    for (collectable in collectableList) {
      anchor.pose.toMatrix(modelMatrix, 0)

      Matrix.translateM(modelMatrix, 0, collectable.x, collectable.y, collectable.z)

      // Calculate model/view/projection matrices
      Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
      Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

      // Update shader properties and draw
      virtualObjectShader.setMat4("u_ModelView", modelViewMatrix)
      virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
      val texture =
        if ((trackable as? InstantPlacementPoint)?.trackingMethod ==
          InstantPlacementPoint.TrackingMethod.SCREENSPACE_WITH_APPROXIMATE_DISTANCE
        ) {
          virtualObjectAlbedoInstantPlacementTexture
        } else {
          virtualObjectAlbedoTexture
        }
      virtualObjectShader.setTexture("u_AlbedoTexture", texture)




      if (activity.currentShapeFarm == "Cube")
      {
        render.draw(virtualObjectMeshCube, virtualObjectShader, virtualSceneFramebuffer)
      }
      else if (activity.currentShapeFarm == "Pyramid")
      {
        render.draw(virtualObjectMeshPyramid, virtualObjectShader, virtualSceneFramebuffer)
      }
      else
      {
        // Fallback
        render.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer)
      }
    }
  }

  // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
  private fun handleTap(frame: Frame, camera: Camera) {
    if (camera.trackingState != TrackingState.TRACKING) return
    val tap = activity.view.tapHelper.poll() ?: return

    //tap.offsetLocation(50.0f, 10.0f)

    val hitResultList =
      if (activity.instantPlacementSettings.isInstantPlacementEnabled) {
        frame.hitTestInstantPlacement(tap.x, tap.y, APPROXIMATE_DISTANCE_METERS)
      } else {
        frame.hitTest(tap)
      }

    // Hits are sorted by depth. Consider only closest hit on a plane, Oriented Point, Depth Point,
    // or Instant Placement Point.
    val firstHitResult =
      hitResultList.firstOrNull { hit ->
        when (val trackable = hit.trackable!!) {
          is Plane ->
            trackable.isPoseInPolygon(hit.hitPose) &&
              PlaneRenderer.calculateDistanceToPlane(hit.hitPose, camera.pose) > 0
          is Point -> trackable.orientationMode == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL
          is InstantPlacementPoint -> true
          // DepthPoints are only returned if Config.DepthMode is set to AUTOMATIC.
          is DepthPoint -> true
          else -> false
        }
      }

    if (firstHitResult != null) {
      // Cap the number of objects created. This avoids overloading both the
      // rendering system and ARCore.

      if (wrappedAnchors.size >= 20) {
        wrappedAnchors[0].anchor.detach()
        wrappedAnchors.removeAt(0)
      }

      // TODO: Move this constant out
      // Radius threshold as we create it as circle/point distance check
      val hitDistanceThresholdMeters = 0.5f
      val hitPose = firstHitResult.hitPose
      val hitPoseTranslation = hitPose.translation

      var addHit : Boolean = true


      //Check if hit result is near a collectable object.
      //We should assume only one anchor for simplicity...
      for ((anchor, trackable) in
      wrappedAnchors.filter { it.anchor.trackingState == TrackingState.TRACKING }) {
        //Clone to prevent concurrency issues again... N is small size so its ok...
        //easy fix since N is small is to clone the list
        synchronized(collectableList) {
          val iterator = collectableList.iterator()
          while (iterator.hasNext()) {
            val collectable = iterator.next()

            // Assumption: the hit pose and the anchor are in the same world coordinate space...
            val dx: Float = hitPose.tx() - collectable.x
            val dy: Float = 0.0f //Don't stress about the floor
            val dz: Float = hitPose.tz() - collectable.z

            // Compute the straight-line distance.
            val distanceMeters = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
            Log.d("Debug Hit Detection", "distanceMeters: ${distanceMeters}")
            if (distanceMeters < collectable.radius)
            {
              listener.onObjectTapped(1)
              soundEffectsManager.playCollectSound()
              activity.updateShapeCount()
              Log.d("Debug Hit Detection", "Hit Detected for object!")
              iterator.remove()
            }
            else
            {
              Log.d("Debug Hit Detection", "distanceMeters: ${distanceMeters}")
              Log.d("Debug Hit Detection", "tap x,y,z: ${hitPose.tx()}, ${hitPose.ty()}, ${hitPose.tz()}")
              Log.d("Debug Hit Detection", "collectable x,y,z: ${collectable.x}, ${collectable.y}, ${collectable.z}")
            }
          }
        }
      }


      //TODO: Check if hit result is near an anchor. If so, get the nearest anchor
      for ((anchor, trackable, farmData) in
        wrappedAnchors.filter { it.anchor.trackingState == TrackingState.TRACKING }) {

        // https://stackoverflow.com/questions/45982196/how-to-measure-distance-using-arcore
        // I only googled because 'surely got 'get distance function' but there is not
        // (Yea I also shocked ARCore got not math library lol)
        // Compute the difference vector between the two hit locations.
        // Compute the difference vector between the two hit locations.
        val endPose = anchor.pose
        val dx: Float = hitPose.tx() - endPose.tx()
        val dy: Float = hitPose.ty() - endPose.ty()
        val dz: Float = hitPose.tz() - endPose.tz()

        // Compute the straight-line distance.
        val distanceMeters = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
        if (distanceMeters < hitDistanceThresholdMeters)
        {
          // I just realized I need to add to the anchor class an ID indicating which
          // farm it belongs to
          addHit = false
          //listener.onObjectTapped(1)
          Log.d("Debug Hit Detection", "Hit Detected at: ${farmData.uid}")
        }
      }

      if (addHit)
      {
        //addFarmPrototypeTest(hitPose)

        //farm data not relevant anymore btw
        //Only allow one anchor at a time.
        // TODO: Remove the anchor whenever we leave the activity
        if (wrappedAnchors.isEmpty())
        {
          wrappedAnchors.add(WrappedAnchor(firstHitResult.createAnchor(), firstHitResult.trackable, FarmData(0)))
          //playObjectPlacedSound()

          //Prototype offset function
          //var offsetX = 0.5f
          var offsetZ = 0.5f

          activity.setCollectableTaskRun()

          //Offset from anchor
          //createCollectable(firstHitResult.hitPose.tx() + offsetX, firstHitResult.hitPose.ty(), firstHitResult.hitPose.tz() + offsetZ)
        }

      }

      //addFarmToDatabase()
      // For devices that support the Depth API, shows a dialog to suggest enabling
      // depth-based occlusion. This dialog needs to be spawned on the UI thread.
      activity.runOnUiThread { activity.view.showOcclusionDialogIfNeeded() }
    }
  }

  public fun addAnchorGPS(anchor : Anchor)
  {
    gpsAnchors.add(anchor)
  }

  // Create a new collectable and add it the list
  public fun createCollectable(offsetX : Float, offsetY : Float, offsetZ : Float) {

    var maxCollectableNumber = 6
    synchronized(wrappedAnchors)
    {
      synchronized(collectableList)
      {
        if (wrappedAnchors.isNotEmpty() && collectableList.size < maxCollectableNumber)
        {
          var anchor = wrappedAnchors.first().anchor
          var translate = anchor.pose.translation

          var collectable = CollectableObject(translate[0] + offsetX, translate[1] + offsetY,
            translate[2] + offsetZ)

          // TODO: Don't spawn if too near another collectable.

          //Sometimes causes a crash because of iteration in the rendering loop...
          collectableList.add(collectable)
          playObjectPlacedSound()
        }
      }
    }
  }


  // Old code from testing not sure if needed anymore
  private fun addFarmPrototypeTest(hitPose : Pose)
  {
    // Adding an Anchor tells ARCore that it should track this position in
    // space. This anchor is created on the Plane to place the 3D model
    // in the correct position relative both to the world and to the plane.

    var pose : GeospatialPose = activity.earth.getGeospatialPose(hitPose)
    Log.d("Hit Result (Geospatial Pose)", "longitude: ${pose.longitude}")
    Log.d("Hit Result (Geospatial Pose)", "latitude: ${pose.latitude}")
    Log.d("Hit Result (Geospatial Pose)", "altitude: ${pose.altitude}")
    Log.d("Hit Result (Geospatial Pose)", "eastUpSouthQuaternion : ${pose.eastUpSouthQuaternion}")

    val newFarm = FarmItem(name = "Test Farm", lat = pose.latitude, long =  pose.longitude, alt = pose.altitude,
      qx_set = pose.eastUpSouthQuaternion[0], qy_set = pose.eastUpSouthQuaternion[1], qz_set = pose.eastUpSouthQuaternion[2], qw_set = pose.eastUpSouthQuaternion[3])

    var uid : Long = 0L
    runBlocking {
      val job = launch {
        uid = activity.addFarm(newFarm)
      }
      job.join()

      Log.d("Hit Result (Geospatial Pose)", "UID: ${uid}")
    }
  }


  public fun clearAnchorGPS()
  {
    gpsAnchors.clear()
  }

  public fun removeAnchors()
  {
    wrappedAnchors.clear()
  }

  public fun showMinigameEndText()
  {
    //SPAGHETTI LOGIC: THIS IS CALLED **BEFORE** removeAnchors in HelloArActivity
    //THAT IS WHY I CAN ASSUME THE ANCHOR LIST NOT CLEARED YET

    //THIS IS SO THAT THE MESSAGE DOES NOT APPEAR DURING THE STARTUP WHERE THE THING
    //GOES ITS A BUG AND I JUST HAVE TO WORK AROUND IT
    if (wrappedAnchors.isEmpty())
    {
      return
    }

    //I rushing stuff at like 10.47pm on 28/2/2024 I don't have time to make this make sense
    //val message: String = "End of the minigame! Tap again to collect more!"
    //activity.view.snackbarHelper.showMessage(activity, message)

    // Use toast as placeholder
    // as snackbarHelper would need some kind of timer thing and its just... I don't have time now
    //activity.applicationContext.Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    activity.displayMinigameEndMessage()
  }

  public fun isAnchorEmpty() : Boolean
  {
    return wrappedAnchors.isEmpty()
  }

  public fun removeCollectables()
  {
    collectableList.clear()
  }

  private fun showError(errorMessage: String) =
    activity.view.snackbarHelper.showError(activity, errorMessage)

  private fun playObjectPlacedSound() {
//    val randomIndex = Random().nextInt(audioResources.size)
//    val audioResource = audioResources[randomIndex]

    soundEffectsManager.playRandomSound()

//    mediaPlayer?.release()
//
//    mediaPlayer = MediaPlayer.create(activity.applicationContext, audioResource)
//    mediaPlayer?.setOnCompletionListener {
//      it.release()
//    }
//    mediaPlayer?.start()
  }
}



data class FarmData (
  val uid: Long
)

/**
 * Associates an Anchor with the trackable it was attached to. This is used to be able to check
 * whether or not an Anchor originally was attached to an {@link InstantPlacementPoint}.
 *
 * Edit from Clem: I also had to add an additional data to indicate the farm specifications
 * So that it can interact with database
 */
private data class WrappedAnchor(
  val anchor: Anchor,
  val trackable: Trackable,
  val farmData : FarmData
)
