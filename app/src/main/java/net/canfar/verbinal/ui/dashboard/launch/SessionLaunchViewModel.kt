package net.canfar.verbinal.ui.dashboard.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.canfar.verbinal.data.model.ImageParser
import net.canfar.verbinal.data.model.ParsedImage
import net.canfar.verbinal.data.model.RecentLaunch
import net.canfar.verbinal.data.model.SessionLaunchParams
import net.canfar.verbinal.data.repository.ImageRepository
import net.canfar.verbinal.data.repository.RecentLaunchRepository
import net.canfar.verbinal.data.repository.SessionRepository
import javax.inject.Inject

data class SessionLaunchUiState(
    val selectedType: String = "notebook",
    val selectedProject: String = "",
    val selectedImage: ParsedImage? = null,
    val sessionName: String = "",
    val resourceType: String = "flexible",
    val cores: Int = 2,
    val ram: Int = 8,
    val gpus: Int = 0,
    val isLoading: Boolean = false,
    val isLaunching: Boolean = false,
    val launchStatus: String = "",
    val launchSuccess: Boolean = false,
    val errorMessage: String = "",
    val hasError: Boolean = false,
    val customImageUrl: String = "",
    val repositoryHost: String = "images.canfar.net",
    val repositoryUsername: String = "",
    val repositorySecret: String = "",
    val useCustomImage: Boolean = false,
    val isAtSessionLimit: Boolean = false,
    val sessionLimitMessage: String = "",
    val sessionTypes: List<String> = listOf("notebook", "desktop", "carta", "contributed", "firefly"),
    val projects: List<String> = emptyList(),
    val images: List<ParsedImage> = emptyList(),
    val coreOptions: List<Int> = emptyList(),
    val ramOptions: List<Int> = emptyList(),
    val gpuOptions: List<Int> = emptyList(),
    val repositories: List<String> = emptyList(),
)

@HiltViewModel
class SessionLaunchViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val imageRepository: ImageRepository,
    private val recentLaunchRepository: RecentLaunchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionLaunchUiState())
    val uiState: StateFlow<SessionLaunchUiState> = _uiState.asStateFlow()

    private var imagesByTypeAndProject: Map<String, Map<String, List<ParsedImage>>> = emptyMap()
    private var sessionCounter: ((String) -> Int)? = null
    private var totalSessionCounter: (() -> Int)? = null

    companion object {
        private const val MAX_CONCURRENT_SESSIONS = 3
        private val DEFAULT_IMAGE_NAMES = mapOf(
            "notebook" to "astroml:latest",
            "desktop" to "desktop:latest",
            "carta" to "carta:latest",
            "contributed" to "astroml-vscode:latest",
            "firefly" to "firefly:2025.2",
        )
    }

    fun setSessionCounter(counter: (String) -> Int) {
        sessionCounter = counter
    }
    fun setTotalSessionCounter(counter: () -> Int) {
        totalSessionCounter = counter
    }

    fun loadImagesAndContext() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val rawImages = imageRepository.getImages()
                imagesByTypeAndProject = ImageParser.groupByTypeAndProject(rawImages)

                val repos = imageRepository.getRepositories()
                val context = imageRepository.getContext()

                var coreOpts = emptyList<Int>()
                var ramOpts = emptyList<Int>()
                var gpuOpts = emptyList<Int>()
                var defaultCores = 2
                var defaultRam = 8

                if (context != null) {
                    coreOpts = context.cores.options
                    defaultCores = context.cores.default_
                    ramOpts = context.memoryGB.options
                    defaultRam = context.memoryGB.default_
                    gpuOpts = if (0 !in context.gpus.options) listOf(0) + context.gpus.options else context.gpus.options
                }

                _uiState.value = _uiState.value.copy(
                    repositories = repos,
                    repositoryHost = repos.firstOrNull() ?: "images.canfar.net",
                    coreOptions = coreOpts,
                    cores = defaultCores,
                    ramOptions = ramOpts,
                    ram = defaultRam,
                    gpuOptions = gpuOpts,
                    isLoading = false,
                )

                updateProjects()
                generateSessionName()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load images: ${e.message}",
                    hasError = true,
                )
            }
        }
    }

    fun onTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        updateProjects()
        generateSessionName()
    }

    fun onProjectChanged(project: String) {
        _uiState.value = _uiState.value.copy(selectedProject = project)
        updateImages()
    }

    fun onImageChanged(image: ParsedImage?) {
        _uiState.value = _uiState.value.copy(selectedImage = image)
    }

    fun onSessionNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(sessionName = name)
    }

    fun onResourceTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(resourceType = type)
    }

    fun onCoresChanged(cores: Int) {
        _uiState.value = _uiState.value.copy(cores = cores)
    }
    fun onRamChanged(ram: Int) {
        _uiState.value = _uiState.value.copy(ram = ram)
    }
    fun onGpusChanged(gpus: Int) {
        _uiState.value = _uiState.value.copy(gpus = gpus)
    }
    fun onCustomImageUrlChanged(url: String) {
        _uiState.value = _uiState.value.copy(customImageUrl = url)
    }
    fun onRepositoryHostChanged(host: String) {
        _uiState.value = _uiState.value.copy(repositoryHost = host)
    }
    fun onRepositoryUsernameChanged(u: String) {
        _uiState.value = _uiState.value.copy(repositoryUsername = u)
    }
    fun onRepositorySecretChanged(s: String) {
        _uiState.value = _uiState.value.copy(repositorySecret = s)
    }
    fun onUseCustomImageChanged(v: Boolean) {
        _uiState.value = _uiState.value.copy(useCustomImage = v)
    }

    private fun updateProjects() {
        val type = _uiState.value.selectedType
        val projects = imagesByTypeAndProject[type]?.keys?.sorted() ?: emptyList()
        val defaultProject = findProjectWithDefaultImage(type)
        val selected = defaultProject ?: projects.firstOrNull() ?: ""
        _uiState.value = _uiState.value.copy(projects = projects, selectedProject = selected)
        updateImages()
    }

    private fun findProjectWithDefaultImage(type: String): String? {
        val defaultName = DEFAULT_IMAGE_NAMES[type] ?: return null
        val projects = imagesByTypeAndProject[type] ?: return null
        for ((project, images) in projects) {
            if (images.any { it.id.endsWith(defaultName, ignoreCase = true) || it.label.equals(defaultName, ignoreCase = true) }) {
                return project
            }
        }
        return null
    }

    private fun updateImages() {
        val state = _uiState.value
        val images = imagesByTypeAndProject[state.selectedType]?.get(state.selectedProject) ?: emptyList()
        val selected = trySelectDefaultImage(images, state.selectedType) ?: images.firstOrNull()
        _uiState.value = _uiState.value.copy(images = images, selectedImage = selected)
    }

    private fun trySelectDefaultImage(images: List<ParsedImage>, type: String): ParsedImage? {
        val defaultName = DEFAULT_IMAGE_NAMES[type] ?: return null
        return images.firstOrNull { it.label.equals(defaultName, ignoreCase = true) }
            ?: images.firstOrNull { it.id.endsWith(defaultName, ignoreCase = true) }
    }

    fun generateSessionName() {
        val type = _uiState.value.selectedType
        val count = sessionCounter?.invoke(type) ?: 0
        _uiState.value = _uiState.value.copy(sessionName = "$type${count + 1}")
    }

    fun updateSessionLimit() {
        val count = totalSessionCounter?.invoke() ?: 0
        val atLimit = count >= MAX_CONCURRENT_SESSIONS
        _uiState.value = _uiState.value.copy(
            isAtSessionLimit = atLimit,
            sessionLimitMessage = if (atLimit) "Session limit reached ($count/$MAX_CONCURRENT_SESSIONS). Delete a session to launch a new one." else "",
        )
    }

    fun launch() {
        updateSessionLimit()
        val state = _uiState.value
        if (state.isAtSessionLimit) {
            _uiState.value = state.copy(errorMessage = state.sessionLimitMessage, hasError = true)
            return
        }

        val imageToLaunch: String
        if (state.useCustomImage) {
            if (state.customImageUrl.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Please provide a container image URL.", hasError = true)
                return
            }
            imageToLaunch = "${state.repositoryHost}/${state.customImageUrl}"
        } else {
            if (state.selectedImage == null) {
                _uiState.value = state.copy(errorMessage = "Please select a container image.", hasError = true)
                return
            }
            imageToLaunch = state.selectedImage.id
        }

        if (state.sessionName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please provide a session name.", hasError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLaunching = true, launchStatus = "Requesting session...", hasError = false, launchSuccess = false)

            try {
                val params = SessionLaunchParams(
                    type = state.selectedType,
                    name = state.sessionName,
                    image = imageToLaunch,
                    cores = if (state.resourceType == "fixed") state.cores else 0,
                    ram = if (state.resourceType == "fixed") state.ram else 0,
                    gpus = if (state.resourceType == "fixed") state.gpus else 0,
                    registryUsername = if (state.useCustomImage) state.repositoryUsername else null,
                    registrySecret = if (state.useCustomImage) state.repositorySecret else null,
                )

                val recentLaunch = RecentLaunch(
                    name = state.sessionName,
                    type = state.selectedType,
                    image = imageToLaunch,
                    imageLabel = if (state.useCustomImage) state.customImageUrl else state.selectedImage?.label ?: imageToLaunch,
                    project = if (state.useCustomImage) "" else state.selectedProject,
                    resourceType = state.resourceType,
                    cores = state.cores,
                    ram = state.ram,
                    gpus = state.gpus,
                    launchedAt = System.currentTimeMillis(),
                )

                val sessionId = sessionRepository.launchSession(params)
                if (sessionId != null) {
                    _uiState.value = _uiState.value.copy(
                        launchStatus = "Session launched successfully!",
                        launchSuccess = true,
                        isLaunching = false,
                    )
                    generateSessionName()
                    recentLaunchRepository.save(recentLaunch)
                } else {
                    _uiState.value = _uiState.value.copy(
                        launchStatus = "Failed to launch session.",
                        hasError = true,
                        isLaunching = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Launch failed",
                    launchStatus = "Launch failed.",
                    hasError = true,
                    isLaunching = false,
                )
            }
        }
    }

    fun relaunch(launch: RecentLaunch) {
        updateSessionLimit()
        val state = _uiState.value
        if (state.isAtSessionLimit) {
            _uiState.value = state.copy(errorMessage = state.sessionLimitMessage, hasError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLaunching = true, launchStatus = "Requesting session...", hasError = false, launchSuccess = false)
            try {
                val params = SessionLaunchParams(
                    type = launch.type,
                    name = launch.name,
                    image = launch.image,
                    cores = if (launch.resourceType == "fixed") launch.cores else 0,
                    ram = if (launch.resourceType == "fixed") launch.ram else 0,
                    gpus = if (launch.resourceType == "fixed") launch.gpus else 0,
                )

                val sessionId = sessionRepository.launchSession(params)
                if (sessionId != null) {
                    _uiState.value = _uiState.value.copy(launchStatus = "Session launched successfully!", launchSuccess = true, isLaunching = false)
                    recentLaunchRepository.save(launch.copy(launchedAt = System.currentTimeMillis()))
                } else {
                    _uiState.value = _uiState.value.copy(launchStatus = "Failed to launch session.", hasError = true, isLaunching = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Launch failed", launchStatus = "Launch failed.", hasError = true, isLaunching = false)
            }
        }
    }

    fun clearLaunchResult() {
        _uiState.value = _uiState.value.copy(launchSuccess = false, launchStatus = "", hasError = false, errorMessage = "")
    }
}
