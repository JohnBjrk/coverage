package jb.coverboard.coverboardwebapp

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<RegisteredUser, Int> {
    fun getByUserId(userId: String): RegisteredUser?
}