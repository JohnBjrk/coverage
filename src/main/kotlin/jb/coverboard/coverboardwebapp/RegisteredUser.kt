package jb.coverboard.coverboardwebapp

import javax.persistence.*

@Entity
data class RegisteredUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(unique = true)
    val userId: String,

    @Column
    val accessToken: String,

    @Column(nullable = false)
    val refreshToken: String,

    @Column(nullable = false)
    val active: Boolean = true
)