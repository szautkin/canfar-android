package net.canfar.verbinal.ui.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import net.canfar.verbinal.data.repository.RecentLaunchRepository
import javax.inject.Inject

@HiltViewModel
class DashboardRecentHelper
@Inject
constructor(
    val repo: RecentLaunchRepository,
) : ViewModel()
