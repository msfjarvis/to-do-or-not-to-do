package dev.msfjarvis.lobsters.injection

import android.util.Log
import com.squareup.moshi.Moshi
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.msfjarvis.lobsters.data.api.LobstersApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

  @Provides
  @BaseUrlQualifier
  fun provideBaseUrl(): String {
    return LobstersApi.BASE_URL
  }

  @Provides
  fun provideClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .addNetworkInterceptor { chain ->
        val request = chain.request()
        Log.d("LobstersApi", "${request.method()}: ${request.url()}")
        chain.proceed(request)
      }
      .build()
  }

  /**
   * Using [Lazy] here is a trick I picked up from Zac Sweers, which he explained in more detail
   * here: https://www.zacsweers.dev/dagger-party-tricks-deferred-okhttp-init/
   */
  @Provides
  fun provideRetrofit(
    client: Lazy<OkHttpClient>,
    moshi: Lazy<Moshi>,
    @BaseUrlQualifier baseUrl: String
  ): Retrofit {
    return Retrofit.Builder()
      .client(client.get())
      .baseUrl(baseUrl)
      .addConverterFactory(MoshiConverterFactory.create(moshi.get()))
      .build()
  }

  @Provides
  fun provideApi(retrofit: Retrofit): LobstersApi {
    return retrofit.create()
  }
}
