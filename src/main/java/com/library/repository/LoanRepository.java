package com.library.repository;

import com.library.entity.Loan;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Override
    @EntityGraph(attributePaths = {"book", "reader"})
    Page<Loan> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"book", "reader"})
    @Query("select l from Loan l where l.id = :id")
    Optional<Loan> findWithDetails(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Loan l where l.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"book", "reader"})
    @Query("select l from Loan l where l.returnedAt is null")
    Page<Loan> findActive(Pageable pageable);

    @EntityGraph(attributePaths = {"book", "reader"})
    @Query("select l from Loan l where l.returnedAt is null and l.dueDate < :today")
    Page<Loan> findOverdue(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = {"book", "reader"})
    @Query("select l from Loan l where l.returnedAt is not null")
    Page<Loan> findReturned(Pageable pageable);

    @Query("""
           select l from Loan l join fetch l.book join fetch l.reader
           where l.returnedAt is null and l.dueDate < :today
           """)
    List<Loan> findAllOverdue(@Param("today") LocalDate today);

    long countByReaderIdAndReturnedAtIsNull(Long readerId);

    boolean existsByReaderIdAndReturnedAtIsNullAndDueDateBefore(Long readerId, LocalDate today);

    boolean existsByBookId(Long bookId);

    boolean existsByReaderId(Long readerId);
}
