package controllers;

import entities.jsonModel.PostDto;
import models.Post;
import models.User;
import play.Logger;
import repositories.PostRepository;
import repositories.impl.PostRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class PostController extends BaseController {
    private final PostRepository postRepository;

    public PostController() {
        postRepository = new PostRepositoryImpl();
    }

    public void getAll() {
        Logger.info("Call api list post");
        List<PostDto> postDtos = new ArrayList<>();
        List<Post> allPost = postRepository.getAllPost();
        for (Post post : allPost) {
            postDtos.add(new PostDto(post));
        }
        responseSuccess(postDtos);
    }

    public void getById(Integer id) {
        Logger.info("Call api list post by id");
        Post post = Post.findById(id);
        PostDto postDto = new PostDto(post);
        responseSuccess(postDto);
    }
}
