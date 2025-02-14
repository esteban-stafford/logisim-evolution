	.file	"test.c"
	.option nopic
	.attribute arch, "rv32i2p0"
	.attribute unaligned_access, 0
	.attribute stack_align, 16
	.text
	j main
	.globl	io_base
	.section	.sdata,"aw"
	.align	2
	.type	io_base, @object
	.size	io_base, 4
io_base:
	.word	268435456
	.globl	a
	.align	2
	.type	a, @object
	.size	a, 4
a:
	.word	20
	.text
	.align	2
	.globl	func
	.type	func, @function
func:
	addi	sp,sp,-32
	sw	s0,28(sp)
	addi	s0,sp,32
	sw	a0,-20(s0)
	sw	a1,-24(s0)
	lui	a5,%hi(io_base)
	lw	a5,%lo(io_base)(a5)
	lw	a4,-24(s0)
	sw	a4,0(a5)
	nop
	lw	s0,28(sp)
	addi	sp,sp,32
	jr	ra
	.size	func, .-func
	.align	2
	.globl	main
	.type	main, @function
main:
	addi	sp,sp,-16
	sw	ra,12(sp)
	sw	s0,8(sp)
	addi	s0,sp,16
	lui	a5,%hi(io_base)
	lw	a4,%lo(io_base)(a5)
	lui	a5,%hi(a)
	lw	a5,%lo(a)(a5)
	mv	a1,a5
	mv	a0,a4
	call	func
	li	a5,0
	mv	a0,a5
	lw	ra,12(sp)
	lw	s0,8(sp)
	addi	sp,sp,16
	jr	ra
	.size	main, .-main
	.ident	"GCC: (g2ee5e430018) 12.2.0"
