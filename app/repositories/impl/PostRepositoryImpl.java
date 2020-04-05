package repositories.impl;

import javafx.geometry.Pos;
import models.Post;
import repositories.PostRepository;

import java.util.List;

public class PostRepositoryImpl implements PostRepository {
    @Override
    public List<Post> getAllPost(){
        return Post.findAll();
    }
}
