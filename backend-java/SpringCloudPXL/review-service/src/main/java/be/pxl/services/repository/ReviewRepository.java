package be.pxl.services.repository;

import be.pxl.services.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ReviewRepository extends JpaRepository<Review, UUID> { }
