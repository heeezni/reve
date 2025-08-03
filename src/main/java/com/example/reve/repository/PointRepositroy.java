package com.example.reve.repository;

import com.example.reve.domain.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PointRepositroy extends JpaRepository<Point,Long> {
}
