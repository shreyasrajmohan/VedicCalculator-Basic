module vedic_top(
    input  wire [7:0] a,
    input  wire [7:0] b,
    output wire [7:0] sum,
    output wire       sum_cout,
    output wire [7:0] diff,
    output wire       diff_borrow,
    output wire [15:0] product,
    output wire [7:0] quotient,
    output wire [7:0] remainder,
    output wire       divide_by_zero
);
    vedic_adder_8bit adder_u(
        .a(a),
        .b(b),
        .sum(sum),
        .cout(sum_cout)
    );

    vedic_subtractor_8bit subtractor_u(
        .a(a),
        .b(b),
        .diff(diff),
        .borrow(diff_borrow)
    );

    vedic_mult_8x8 multiplier_u(
        .a(a),
        .b(b),
        .product(product)
    );

    vedic_divider_8bit divider_u(
        .dividend(a),
        .divisor(b),
        .quotient(quotient),
        .remainder(remainder),
        .divide_by_zero(divide_by_zero)
    );
endmodule

