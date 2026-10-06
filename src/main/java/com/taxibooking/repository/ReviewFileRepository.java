package com.taxibooking.repository;

import com.taxibooking.model.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * File Repository for Reviews & Ratings (Component 6 - Rating, Reviews & Admin Panel)
 */
@Repository
public class ReviewFileRepository extends AbstractFileRepository<Review, String> {

    public ReviewFileRepository(@Value("${app.data.reviews-file:data/reviews.txt}") String filePath) {
        super(filePath);
    }

    @Override
    protected String getId(Review entity) {
        return entity.getReviewId();
    }

    @Override
    protected String serialize(Review entity) {
        return entity.toFileString();
    }

    @Override
    protected Review deserialize(String line) {
        return Review.fromFileString(line);
    }
}
