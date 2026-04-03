module vedic_divider_8bit(
    input  wire [7:0] dividend,
    input  wire [7:0] divisor,
    output reg  [7:0] quotient,
    output reg  [7:0] remainder,
    output reg        divide_by_zero
);
    integer i;
    reg [8:0] rem_work;

    always @(*) begin
        quotient       = 8'b00000000;
        remainder      = 8'b00000000;
        divide_by_zero = (divisor == 8'b00000000);
        rem_work       = 9'b000000000;

        if (!divide_by_zero) begin
            for (i = 7; i >= 0; i = i - 1) begin
                rem_work = {rem_work[7:0], dividend[i]};
                if (rem_work >= {1'b0, divisor}) begin
                    rem_work    = rem_work - {1'b0, divisor};
                    quotient[i] = 1'b1;
                end
            end
            remainder = rem_work[7:0];
        end
    end
endmodule

