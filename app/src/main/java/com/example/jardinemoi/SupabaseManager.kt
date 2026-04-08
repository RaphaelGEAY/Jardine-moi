package com.example.jardinemoi

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth

object SupabaseManager {

    lateinit var client: SupabaseClient

    fun init() {
        client = createSupabaseClient(
            supabaseUrl = "https://mhvqouoferviewbgsxka.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1odnFvdW9mZXJ2aWV3YmdzeGthIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU2MjYxNjUsImV4cCI6MjA5MTIwMjE2NX0.Z6JrMTwvNRx15fWVvmMuNeif_RZV3FE6O3N5Akudfgo" // ✔️ ANON KEY
        ) {
            install(Auth)
        }
    }
}
