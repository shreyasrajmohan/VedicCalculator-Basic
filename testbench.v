`timescale 1ns/1ps
`include "primitives.v"
`include "vedic_adder_8bit.v"
`include "vedic_subtractor_8bit.v"
`include "vedic_mult_4x4.v"
`include "vedic_mult_8x8.v"
`include "vedic_divider_8bit.v"
`include "vedic_top.v"

module testbench;
    reg  [7:0] a;
    reg  [7:0] b;
    wire [7:0] sum;
    wire       sum_cout;
    wire [7:0] diff;
    wire       diff_borrow;
    wire [15:0] product;
    wire [7:0] quotient;
    wire [7:0] remainder;
    wire       divide_by_zero;

    vedic_top dut(
        .a(a),
        .b(b),
        .sum(sum),
        .sum_cout(sum_cout),
        .diff(diff),
        .diff_borrow(diff_borrow),
        .product(product),
        .quotient(quotient),
        .remainder(remainder),
        .divide_by_zero(divide_by_zero)
    );

    task apply_case;
        input [7:0] ta;
        input [7:0] tb;
        begin
            a = ta;
            b = tb;
            #10;
            $display("a=%0d b=%0d | sum=%0d cout=%b | diff=%0d borrow=%b | product=%0d | quotient=%0d remainder=%0d dbz=%b",
                     a, b, sum, sum_cout, diff, diff_borrow, product, quotient, remainder, divide_by_zero);
        end
    endtask

    initial begin
        $display("Starting Vedic Calculator RTL simulation");
        apply_case(8'd12, 8'd5);
        apply_case(8'd99, 8'd27);
        apply_case(8'd255, 8'd15);
        apply_case(8'd64, 8'd8);
        apply_case(8'd23, 8'd0);
        $display("Simulation complete");
        $finish;
    end
endmodule
