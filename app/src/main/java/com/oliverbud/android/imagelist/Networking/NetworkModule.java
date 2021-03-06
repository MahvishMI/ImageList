package com.oliverbud.android.imagelist.Networking;

import com.oliverbud.android.imagelist.Networking.NetworkManager;
import com.oliverbud.android.imagelist.Networking.ImageApi;
import com.oliverbud.android.imagelist.UI.MainActivity;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by oliverbudiardjo on 6/12/15.
 */
@Module(
        injects = MainActivity.class,
        library = true

)
public class NetworkModule {

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(){
        return new OkHttpClient();
    }

    @Provides
    @Singleton
    public ImageApi provideImageApi(OkHttpClient client){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://ajax.googleapis.com")
                .setClient(new OkClient(client))
                .setConverter(new CustomConverter())
                .build();
        return restAdapter.create(ImageApi.class);
    }

    @Provides
    @Singleton
    public PingApi providePingApi(OkHttpClient client){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://jsonplaceholder.typicode.com")
                .setClient(new OkClient(client))
                .build();
        return restAdapter.create(PingApi.class);
    }

    @Provides @Singleton public NetworkManager provideNetworkManager(ImageApi imageService, PingApi pingService) {

        return new NetworkManager(imageService, pingService);
    }
}
