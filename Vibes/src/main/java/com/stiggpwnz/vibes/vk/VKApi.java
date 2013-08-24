package com.stiggpwnz.vibes.vk;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

import com.stiggpwnz.vibes.vk.models.NewsFeed;

public interface VKApi {

	public static final String SERVER = "https://api.vk.com/method";

	@GET("/newsfeed.get?filters=post&count=100")
	public void getNewsFeed(Callback<NewsFeed.Result> cb);

	@GET("/newsfeed.get?filters=post&count=100")
	public NewsFeed.Result getNewsFeed(@Query("offset") int offset);
}