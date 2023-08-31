package org.junnikym.springfbp.filter

@Target(allowedTargets = [AnnotationTarget.CLASS, AnnotationTarget.FUNCTION])
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreManage
