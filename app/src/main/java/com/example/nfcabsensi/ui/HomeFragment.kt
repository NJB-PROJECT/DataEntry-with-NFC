package com.example.nfcabsensi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nfcabsensi.R
import com.example.nfcabsensi.data.database.AppDatabase
import com.example.nfcabsensi.data.repository.AppRepository
import com.example.nfcabsensi.databinding.FragmentHomeBinding
import com.example.nfcabsensi.ui.viewmodel.EventViewModel
import com.example.nfcabsensi.ui.viewmodel.EventViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val repository = AppRepository(db.studentDao(), db.eventDao(), db.studentEventDao())
        val factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        val adapter = EventAdapter { event ->
            val bundle = Bundle().apply {
                putInt("eventId", event.id)
            }
            findNavController().navigate(R.id.action_homeFragment_to_eventDetailFragment, bundle)
        }

        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.adapter = adapter

        lifecycleScope.launch {
            viewModel.allEvents.collectLatest { events ->
                if (events.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvEvents.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvEvents.visibility = View.VISIBLE
                    adapter.submitList(events)
                }
            }
        }

        binding.btnCreateEvent.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addEventFragment)
        }

        // Add a temporary or permanent way to access student list.
        // For now, let's use a menu provider.
        val menuHost: androidx.core.view.MenuHost = requireActivity()
        menuHost.addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menu.add(0, 1, 0, "Daftar Mahasiswa").setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return if (menuItem.itemId == 1) {
                    findNavController().navigate(R.id.action_homeFragment_to_studentListFragment)
                    true
                } else {
                    false
                }
            }
        }, viewLifecycleOwner, androidx.lifecycle.Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
